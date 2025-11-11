import React, { useState, useRef, useEffect } from 'react'

const WS_URL = `ws://${window.location.hostname}:8080`;
const UPLOAD_URL = `http://${window.location.hostname}:8000/upload`;

export default function App() {
  const [username, setUsername] = useState('');
  const [connected, setConnected] = useState(false);
  const [messages, setMessages] = useState([]);
  const [text, setText] = useState('');
  const wsRef = useRef(null);

  useEffect(() => {
    return () => {
      if (wsRef.current) wsRef.current.close();
    }
  }, [])

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
      } catch (e) { console.error(e) }
    }
    ws.onclose = () => setConnected(false);
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
    const resp = await fetch(UPLOAD_URL, {
      method: 'POST',
      headers: { 'X-Filename': file.name },
      body: file
    });
    const json = await resp.json();
    // Notify chat with file URL
    send({ type: 'BROADCAST', sender: username, content: `File uploaded: ${json.url}` });
  }

  return (
    <div style={{ padding: 16, fontFamily: 'Arial, sans-serif' }}>
      <h2>EnhancedChatApp (React)</h2>

      {!connected ? (
        <div>
          <input placeholder="Your name" value={username} onChange={e=>setUsername(e.target.value)} />
          <button onClick={connect} disabled={!username}>Connect</button>
        </div>
      ) : (
        <div>
          <div style={{ border: '1px solid #ccc', height: 300, overflow: 'auto', padding: 8 }}>
            {messages.map((m, i) => (
              <div key={i}><strong>{m.sender}:</strong> {m.content}</div>
            ))}
          </div>

          <form onSubmit={submitMessage} style={{ marginTop: 8 }}>
            <input style={{ width: '70%' }} value={text} onChange={e=>setText(e.target.value)} />
            <button type="submit">Send</button>
          </form>

          <div style={{ marginTop: 8 }}>
            <label>Upload file: <input type="file" onChange={uploadFile} /></label>
          </div>
        </div>
      )}
    </div>
  )
}
