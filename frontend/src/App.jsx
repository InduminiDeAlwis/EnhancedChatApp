import React, { useState, useRef, useEffect } from 'react'

const WS_URL = `ws://${window.location.hostname}:8080`;
const UPLOAD_URL = `http://${window.location.hostname}:9000/upload`;

function MessageBubble({ m, me }) {
  const cls = m.sender === '[system]' ? 'msg-system' : (me ? 'msg-me' : 'msg-they');

  // Check if message contains a file link
  const fileUrlMatch = m.content.match(/http:\/\/[^\s]+\/files\/[^\s]+/);
  const hasFileLink = fileUrlMatch !== null;

  // Extract filename from message (format: "📎 filename — url")
  const filenameMatch = m.content.match(/📎\s+([^\s—]+)/);
  const filename = filenameMatch ? filenameMatch[1] : 'Download File';

  return (
    <div className={`msg ${cls}`}>
      <div className="msg-sender">{m.sender}</div>
      {hasFileLink ? (
        <div className="msg-content">
          <div>📎 Shared a file: <strong>{filename}</strong></div>
          <a
            href={fileUrlMatch[0]}
            target="_blank"
            rel="noopener noreferrer"
            className="file-link"
            download
          >
            🔗 Click to open/download
          </a>
        </div>
      ) : (
        <div className="msg-content">{m.content}</div>
      )}
    </div>
  )
}

export default function App() {
  const [username, setUsername] = useState('');
  const [connected, setConnected] = useState(false);
  const [messages, setMessages] = useState([]);
  const [text, setText] = useState('');
  const [users, setUsers] = useState([]);
  const wsRef = useRef(null);
  const listRef = useRef(null);

  useEffect(() => {
    return () => { if (wsRef.current) wsRef.current.close(); }
  }, [])

  useEffect(() => {
    // keep scrolled to bottom
    if (listRef.current) listRef.current.scrollTop = listRef.current.scrollHeight;
  }, [messages]);

  function connect() {
    const ws = new WebSocket(WS_URL);
    wsRef.current = ws;
    ws.onopen = () => {
      setConnected(true);
      send({ type: 'LOGIN', sender: username, content: '' });
    }
    ws.onmessage = (ev) => {
      try {
        const msg = JSON.parse(ev.data);
        setMessages(m => [...m, msg]);
        // maintain simple user list from LOGIN/BROADCAST sender
        if (msg.type === 'LOGIN' && msg.sender) setUsers(u => Array.from(new Set([...u, msg.sender])));
        if (msg.type === 'LOGOUT' && msg.sender) setUsers(u => u.filter(x => x !== msg.sender));
      } catch (e) { console.error(e) }
    }
    ws.onclose = () => setConnected(false);
    ws.onerror = (e) => console.error('WS error', e);
  }

  function send(obj) {
    if (!wsRef.current || wsRef.current.readyState !== WebSocket.OPEN) return;
    wsRef.current.send(JSON.stringify(obj));
  }

  function submitMessage(e) {
    e.preventDefault();
    if (!text) return;
    send({ type: 'BROADCAST', sender: username, content: text });
    setText('');
  }

  async function uploadFile(e) {
    const file = e.target.files[0];
    if (!file) return;
    try {
      const resp = await fetch(UPLOAD_URL, {
        method: 'POST', headers: { 'X-Filename': file.name }, body: file
      });
      const json = await resp.json();
      send({ type: 'BROADCAST', sender: username, content: `📎 ${file.name} — ${json.url}` });
    } catch (err) { console.error(err); }
  }

  return (
    <div className="app-root">
      <div className="app-header">EnhancedChatApp</div>
      <div className="app-body">
        <aside className="sidebar">
          <div className="login">
            {!connected ? (
              <>
                <input className="input-username" placeholder="Your name" value={username} onChange={e => setUsername(e.target.value)} />
                <button className="btn" onClick={connect} disabled={!username}>Connect</button>
              </>
            ) : (
              <div className="status">Connected as <strong>{username}</strong></div>
            )}
          </div>

          <div className="users">
            <div className="users-title">Active users</div>
            <ul>
              {users.length === 0 && <li className="muted">No users yet</li>}
              {users.map(u => <li key={u}>{u}</li>)}
            </ul>
          </div>
        </aside>

        <main className="chat">
          <div className="msg-list" ref={listRef}>
            {messages.map((m, i) => (
              <MessageBubble key={i} m={m} me={m.sender === username} />
            ))}
          </div>

          <form className="composer" onSubmit={submitMessage}>
            <input className="txt" placeholder="Type a message" value={text} onChange={e => setText(e.target.value)} />
            <label className="file-btn">📎
              <input type="file" onChange={uploadFile} />
            </label>
            <button className="btn send" type="submit">Send</button>
          </form>
        </main>
      </div>
      <div className="app-footer">WebSocket: {connected ? 'connected' : 'disconnected'}</div>
    </div>
  )
}
