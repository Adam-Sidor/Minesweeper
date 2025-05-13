import React, { useEffect, useState } from 'react';
import logo from './logo.svg';
import axios from 'axios';
import './App.css';

type CellState = 'HIDDEN' | 'REVEALED' | 'FLAGGED';

interface Cell {
  hasMine: boolean;
  adjacentMines: number;
  state: CellState;
}

type Board = Cell[][];

function App() {
  const [board, setBoard] = useState<Board>([]);
  const rows = 8;
  const cols = 8;
  const mines = 10;

  const startGame = async () => {
    const res = await axios.post('http://localhost:8080/api/game/start', {
      rows,
      cols,
      mines
    });
    setBoard(res.data.board);
  };

  const handleClick = async (row: number, col: number) => {
    const res = await axios.post(`http://localhost:8080/api/game/reveal`, { row, col });
    setBoard(res.data.board);
  };

  useEffect(() => {
    startGame();
  }, []);
  return (
    <div className="App">
      <h1>Minesweeper</h1>
      <button onClick={startGame}>Restart</button>
      <br></br>
      <div style={{ display: 'inline-block', marginTop: '10px' }}>
        {board.map((row, r) => (
          <div key={r} style={{ display: 'flex' }}>
            {row.map((cell, c) => (
              <button
                key={c}
                onClick={() => handleClick(r, c)}
                style={{
                  width: 30,
                  height: 30,
                  margin: 1,
                  backgroundColor: cell.state === 'REVEALED' ? '#ddd' : '#999',
                }}
              >
                {cell.state === 'REVEALED' && cell.hasMine ? '💣' :
                  cell.state === 'REVEALED' && cell.adjacentMines > 0 ? cell.adjacentMines : ''}
              </button>
            ))}
          </div>
        ))}
      </div>
    </div>
  );
}

export default App;
