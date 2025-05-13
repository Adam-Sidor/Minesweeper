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

interface GameState{
  board: Cell[][];
  status: GameStatus;
}

type Board = Cell[][];

function App() {
  const [board, setBoard] = useState<Board>([]);
  const [gameStatus, setGameStatus] = useState<GameStatus>('IN_PROGRESS')
  const rows = 8;
  const cols = 8;
  const mines = 10;

  const startGame = async () => {
    try {
      const res = await axios.post('http://localhost:8080/api/game/start', {
      rows,
      cols,
      mines
    });
      setBoard(res.data.board);
      setGameStatus('IN_PROGRESS');
    } catch (error) {
      console.error("Error starting the game:", error);
    }
  };

  const revealCell = async (row: number, col: number) => {
    //if(gameStatus !== 'IN_PROGRESS') return;
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
                onClick={() => revealCell (r, c)}
                disabled={gameStatus !== 'IN_PROGRESS'}
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
      <div style={{ marginTop: '20px' }}>
        {gameStatus === 'LOST' && <div className="game-over">Game Over! You hit a mine!</div>}
        {gameStatus === 'WON' && <div className="game-over">Congratulations! You won!</div>}
        {gameStatus === 'IN_PROGRESS' && <div className="game-over">Game in progress</div>}
      </div>
    </div>
  );
}

export default App;
