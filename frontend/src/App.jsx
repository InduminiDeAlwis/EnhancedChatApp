import React, { useState, useRef, useEffect } from 'react'

const WS_URL = `ws://${window.location.hostname}:8080`;
const UPLOAD_URL = `http://${window.location.hostname}:9000/upload`;

function Avatar({ name, size = 36 }){
  const label = name ? name.trim()[0].toUpperCase() : '?';
  return <div className="avatar" title={name} style={{width:size,height:size,lineHeight:`${size}px`}}>{label}</div>
}

function formatTime(ts){
  const d = new Date(ts || Date.now());
  return d.toLocaleTimeString([], {hour:'2-digit',minute:'2-digit'});
}

function MessageBubble({ m, me, showSender }) {
  const cls = m.sender === '[system]' ? 'msg-system' : (me ? 'msg-me' : 'msg-they');
  return (
    <div className={`msg ${cls} ${showSender? 'first-of-group':''}`}>
      {showSender && m.sender !== '[system]' && (
        <div className="msg-header">
          <Avatar name={m.sender} size={32} />
          <div className="msg-meta">
            <div className="msg-sender">{m.sender}</div>
            <div className="msg-time muted">{formatTime(m.receivedAt)}</div>
          </div>
        </div>
      )}

      <div className="msg-content">{m.content}</div>
      {!showSender && <div className="msg-time small">{formatTime(m.receivedAt)}</div>}
    </div>
  )
}

export default function App() {
  const [username, setUsername] = useState('');
  const [showLogin, setShowLogin] = useState(true);
  const [connected, setConnected] = useState(false);
  const [connecting, setConnecting] = useState(false);
  const [retries, setRetries] = useState(0);
  const [messages, setMessages] = useState([]);
  const [text, setText] = useState('');
  const [users, setUsers] = useState([]);
  const [selectedUser, setSelectedUser] = useState('');
  const [statusMsg, setStatusMsg] = useState('');
  const wsRef = useRef(null);
  const listRef = useRef(null);
  const reconnectTimerRef = useRef(null);
  const seenIdsRef = useRef(new Set());

  useEffect(() => {
    return () => { if (wsRef.current) wsRef.current.close(); }
  }, [])

  useEffect(() => {
    // keep scrolled to bottom
    if (listRef.current) listRef.current.scrollTop = listRef.current.scrollHeight;
  }, [messages]);

  function connect() {
    if (!username) return;
    setConnecting(true);
    setStatusMsg('connecting...');
    const attempt = retries + 1;
    setRetries(attempt);
    const ws = new WebSocket(WS_URL);
    wsRef.current = ws;
    ws.onopen = () => {
      setConnected(true);
      setConnecting(false);
      setRetries(0);
      setStatusMsg('connected');
      send({ type: 'LOGIN', sender: username, content: '' });
      // hide login if shown
      setShowLogin(false);
    }
    ws.onmessage = (ev) => {
      try {
        const msg = JSON.parse(ev.data);
        // ignore duplicates if we've already seen this message id
        if (msg.id && seenIdsRef.current.has(msg.id)) return;
        if (msg.id) seenIdsRef.current.add(msg.id);
        // attach a received timestamp for UI
        msg.receivedAt = Date.now();
        setMessages(m => [...m, msg]);
        // maintain simple user list from LOGIN/LOGOUT
        if (msg.type === 'LOGIN' && msg.sender) setUsers(u => Array.from(new Set([...u, msg.sender])));
        if (msg.type === 'LOGOUT' && msg.sender) setUsers(u => u.filter(x => x !== msg.sender));
      } catch (e) { console.error(e) }
    }
    ws.onclose = () => { 
      setConnected(false); 
      setConnecting(false);
      setStatusMsg('disconnected');
      // attempt reconnect with backoff
      const wait = Math.min(30000, 1000 * (2 ** (Math.max(0, retries-1))));
      reconnectTimerRef.current = setTimeout(()=>{
        setStatusMsg(`reconnecting (attempt ${retries+1})`);
        connect();
      }, wait);
    }
    ws.onerror = (e) => { console.error('WS error', e); setStatusMsg('error'); };
  }

  function send(obj) {
    if (!wsRef.current || wsRef.current.readyState !== WebSocket.OPEN) return;
    wsRef.current.send(JSON.stringify(obj));
  }

  function makeId(){
    return `${Date.now().toString(36)}-${Math.random().toString(36).slice(2,9)}`;
  }

  function disconnect(){
    if (reconnectTimerRef.current) { clearTimeout(reconnectTimerRef.current); reconnectTimerRef.current = null }
    if (wsRef.current) { try { wsRef.current.close() } catch(e){} wsRef.current = null }
    setConnected(false); setConnecting(false); setStatusMsg('disconnected');
  }

  function submitMessage(e) {
    e && e.preventDefault();
    if (!text) return;
    const id = makeId();
    const payload = selectedUser ? { id, type: 'PRIVATE', sender: username, targetUser: selectedUser, content: text } : { id, type: 'BROADCAST', sender: username, content: text };
    // mark as seen to avoid handling the server echo as a duplicate
    seenIdsRef.current.add(id);
    // add local echo with timestamp
    const local = { ...payload, receivedAt: Date.now(), local:true };
    setMessages(m => [...m, local]);
    send(payload);
    setText('');
  }

  async function uploadFile(e) {
    const file = e.target.files[0];
    if (!file) return;
    setStatusMsg('uploading...');
    try {
      const resp = await fetch(UPLOAD_URL, {
        method: 'POST', headers: { 'X-Filename': file.name }, body: file
      });
      const json = await resp.json();
      const id = makeId();
      const payload = { id, type: 'BROADCAST', sender: username, content: `📎 ${file.name} — ${json.url}` };
      seenIdsRef.current.add(id);
      // local echo
      setMessages(m => [...m, { ...payload, receivedAt: Date.now(), local:true }]);
      send(payload);
      setStatusMsg('upload success');
    } catch (err) { console.error(err); setStatusMsg('upload failed'); }
    setTimeout(()=>setStatusMsg(''),2000);
  }

  function handleKeyDown(e){
    if (e.key === 'Enter' && !e.shiftKey){
      submitMessage(e);
    }
  }

  return (
    <div className="app-root">
      <div className="app-header">
        <div className="header-left">
          <div className="logo neon">EnhancedChatApp</div>
          <div className="tagline">Connect. Chat. Share.</div>
          <div className="neon-bar" aria-hidden="true" />
        </div>
        <div className="header-right">
          <div className={`status-dot ${connected? 'online':'offline'}`} />
          <div className="header-info muted">{connected ? `Connected — ${users.length} users` : 'Disconnected'}</div>
        </div>
      </div>

      <div className="app-body">
        <aside className="sidebar">
          <div className="login">
            {connected ? (
              <div className="status">Connected as <strong>{username}</strong> <button className="btn small neon-btn" onClick={disconnect}>Disconnect</button></div>
            ) : (
              <div className="status muted">Not connected</div>
            )}
            <div className="conn-controls">
              {connecting && <div className="muted">Connecting... (attempt {retries})</div>}
              {!connected && !connecting && <button className="btn small neon-btn" onClick={()=>{ if(username) connect(); else setShowLogin(true) }}>{username? 'Connect': 'Sign in'}</button>}
              {(!connected && retries>0) && <button className="btn small ghost" onClick={()=>{ if (reconnectTimerRef.current) { clearTimeout(reconnectTimerRef.current); reconnectTimerRef.current = null } connect() }}>Reconnect now</button>}
            </div>
          </div>

          <div className="users">
            <div className="users-title">Active users</div>
            <ul>
              {users.length === 0 && <li className="muted">No users yet</li>}
              {users.map(u => (
                <li key={u} className={u===selectedUser? 'selected':''} onClick={()=>setSelectedUser(u)}>
                  <Avatar name={u} size={28} />
                  <span className="user-name">{u}</span>
                </li>
              ))}
            </ul>
            <div className="users-footer muted">Tip: click a user to send private message</div>
          </div>
        </aside>

        <main className="chat">
          <div className="msg-list" ref={listRef}>
            {messages.map((m,i) => {
              const prev = messages[i-1];
              const showSender = !prev || prev.sender !== m.sender || (m.sender === '[system]');
              return <MessageBubble key={i} m={m} me={m.sender === username} showSender={showSender} />
            })}
          </div>

          <form className="composer" onSubmit={submitMessage}>
            <textarea className="txt" placeholder={selectedUser?`Whisper to ${selectedUser}`:'Type a message'} value={text} onChange={e=>setText(e.target.value)} onKeyDown={handleKeyDown} rows={1} />
            <label className="file-btn">📎
              <input type="file" onChange={uploadFile} />
            </label>
            <button className="btn send" type="submit" disabled={!connected}>Send</button>
          </form>
          {statusMsg && <div className="status-line">{statusMsg}</div>}
        </main>
      </div>
      {/* Login modal */}
      {showLogin && !connected && (
        <div className="modal-backdrop" onClick={()=>{}}>
          <div className="modal" role="dialog" aria-modal="true" onClick={e=>e.stopPropagation()}>
            <h3>Sign in to EnhancedChat</h3>
            <input className="input-username modal-input" placeholder="Enter display name" value={username} onChange={e=>setUsername(e.target.value)} onKeyDown={e=>{ if(e.key==='Enter'){ connect() } }} />
            <div style={{display:'flex',gap:8,marginTop:12}}>
              <button className="btn" onClick={()=>{ if(username) connect() }} disabled={!username}>{connecting? 'Connecting...':'Connect'}</button>
              <button className="btn ghost" onClick={()=>{ setShowLogin(false) }}>Close</button>
            </div>
            <div style={{marginTop:8,fontSize:13,color:'#9aa4b2'}}>WebSocket: {connecting? `Connecting (attempt ${retries})` : (connected? 'Connected':'Disconnected')}</div>
          </div>
        </div>
      )}
      <div className="app-footer">WebSocket: {connected ? 'connected' : 'disconnected'}</div>
    </div>
  )
}
