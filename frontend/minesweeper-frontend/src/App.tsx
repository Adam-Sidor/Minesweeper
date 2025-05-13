import React, { useEffect, useState } from 'react';
import logo from './logo.svg';
import axios from 'axios';
import './App.css';

type CellState = 'HIDDEN' | 'REVEALED' | 'FLAGGED';
type GameStatus = 'IN_PROGRESS' | 'LOST' | 'WON';

interface Cell {
  hasMine: boolean;
  adjacentMines: number;
  state: CellState;
}

type Board = Cell[][];

type RemainingMines = 0;

function App() {
  const [board, setBoard] = useState<Board>([]);
  const [gameStatus, setGameStatus] = useState<GameStatus>();
  const [remainingMines, setRemainingMines] = useState<RemainingMines>();
  const [time, setTime] = useState(0);
  const rows = 10;
  const cols = 25;
  const mines = 50;

  const startGame = async () => {
    try {
      const res = await axios.post('http://localhost:8080/api/game/start', {
        rows,
        cols,
        mines
      });
      setBoard(res.data.board);
      setGameStatus('IN_PROGRESS');
      setRemainingMines(res.data.remainingMines);
      setTime(0);
    } catch (error) {
      console.error("Error starting the game:", error);
    }
  };

  const revealCell = async (row: number, col: number) => {
    try {
      const res = await axios.post('http://localhost:8080/api/game/reveal', { row, col });
      setBoard(res.data.board);
      setGameStatus(res.data.status);

      if (res.data.status === 'LOST') {
        alert('Game Over! You hit a mine!');
      } else if (res.data.status === 'WON') {
        alert('Congratulations! You won the game!');
      }
    } catch (error) {
      console.error("Error revealing cell:", error);
    }
  };

  const flagCell = async (row: number, col: number) => {
    const res = await axios.post('http://localhost:8080/api/game/flag', { row, col });
    setBoard(res.data.board);
    setGameStatus(res.data.status);
    setRemainingMines(res.data.remainingMines);
  };

  useEffect(() => {
    let interval: NodeJS.Timeout | null = null;

    if (gameStatus === 'IN_PROGRESS') {
      interval = setInterval(() => {
        setTime(prevTime => prevTime + 1);
      }, 1000);
    }

    return () => {
      if (interval) clearInterval(interval);
    };
  }, [gameStatus]);
  return (
    <div className="App">
      <h1>Minesweeper</h1>
      <button onClick={startGame}>Restart</button>
      <div>Mines left: {remainingMines}</div>
      <div>Time: {time} sec</div>
      <div style={{ display: 'inline-block', marginTop: '10px' }}>
        {board.map((row, r) => (
          <div key={r} style={{ display: 'flex' }}>
            {row.map((cell, c) => (
              <button
                key={c}
                onClick={() => revealCell(r, c)}
                onContextMenu={(e) => {
                  e.preventDefault();
                  flagCell(r, c);
                }}
                disabled={gameStatus !== 'IN_PROGRESS'}
                style={{
                  width: 30,
                  height: 30,
                  margin: 1,
                  backgroundColor: cell.state === 'REVEALED' ? '#ddd' : '#999',
                }}
              >
                {cell.hasMine && gameStatus === 'LOST' ? '💣' :
                  cell.state === 'REVEALED' && cell.adjacentMines > 0 ? cell.adjacentMines :
                    cell.state === 'FLAGGED' ? '🚩' : ''}
              </button>
            ))}
          </div>
        ))}
      </div>
      <div style={{ marginTop: '20px' }}>
        {gameStatus === 'LOST' && <div className="game-over">Game Over! You hit a mine!</div>}
        {gameStatus === 'WON' && <div className="game-over">Congratulations! You won!</div>}
        {gameStatus === 'IN_PROGRESS' && <div className="game-over">Game in progress</div>}
      </div>
    </div>
  );
}

export default App;
