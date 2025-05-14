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

  const [showDifficultyMenu, setShowDifficultyMenu] = useState(false);
  const [showCustomDifficultyMenu, setShowCustomDifficultyMenu] = useState(false);
  const [gameConfig, setGameConfig] = useState({ rows: 8, cols: 8, mines: 10 });

  const [customRows, setCustomRows] = useState(8);
  const [customCols, setCustomCols] = useState(8);
  const [customMines, setCustomMines] = useState(10);
  const [errorMessage, setErrorMessage] = useState('');


  const getColor = (n: number): string => {
    switch (n) {
      case 1: return 'blue';
      case 2: return 'green';
      case 3: return 'red';
      case 4: return 'purple';
      case 5: return 'yellow';
      case 6: return 'orange';
      case 7: return 'black';
      case 8: return 'gray';
      default: return 'black';
    }
  };


  const startGame = async () => {
    try {
      const res = await axios.post('http://localhost:8080/api/game/start', gameConfig);
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

  const applyCustomDifficulty = () => {
    const totalCells = customRows * customCols;

    if (customRows < 2 || customCols < 2) {
      setErrorMessage('Plansza musi mieć co najmniej 2x2 pola.');
      return;
    }

    if (customRows > 50 || customCols > 50) {
      setErrorMessage('Plansza może mieć maksymalnie 50x50 pól.');
      return;
    }

    if (customMines < 1) {
      setErrorMessage('Musi być przynajmniej 1 mina.');
      return;
    }

    if (customMines >= totalCells) {
      setErrorMessage('Liczba min musi być mniejsza niż liczba wszystkich pól.');
      return;
    }
    localStorage.setItem('minesweeperDifficulty', JSON.stringify({ rows: customRows, cols: customCols, mines: customMines }));
    setGameConfig({ rows: customRows, cols: customCols, mines: customMines });

    setErrorMessage('');
    setShowCustomDifficultyMenu(false);
    setShowDifficultyMenu(false);
  };

  const setDifficulty = (rows: number, cols: number, mines: number) => {
    setGameConfig({ rows, cols, mines });
    setShowDifficultyMenu(false);
    localStorage.setItem('minesweeperDifficulty', JSON.stringify({ rows, cols, mines }));
  }


  useEffect(() => {
    const savedConfig = localStorage.getItem('minesweeperDifficulty');
    if (savedConfig) {
      setGameConfig(JSON.parse(savedConfig));
    }

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
      <button onClick={() => setShowDifficultyMenu(true)}>Poziom trudności</button>
      {showDifficultyMenu &&
        <div>
          <button className='difficultyButton' onClick={() => setDifficulty(9, 9, 10)}>Łatwy</button>
          <button className='difficultyButton' onClick={() => setDifficulty(16, 16, 40)}>Średni</button>
          <button className='difficultyButton' onClick={() => setDifficulty(16, 30, 99)}>Trudny</button>
          <button className='difficultyButton' onClick={() => setShowCustomDifficultyMenu(true)}>Własny</button>
          {
            showCustomDifficultyMenu && <div id='customDifficultyForm'>
              <div style={{ color: 'red' }}>{errorMessage}</div>
              Wiersze
              <input type="number" min='2' max='50' onChange={e => setCustomRows(+e.target.value)} />
              Kolumny
              <input type="number" min='2' max='50' onChange={e => setCustomCols(+e.target.value)} />
              Miny
              <input type="number" min='1' onChange={e => setCustomMines(+e.target.value)} />
              <button onClick={() => applyCustomDifficulty()}>Zatwierdź</button>
            </div>
          }
        </div>}
      <button onClick={startGame}>
        {gameStatus === 'IN_PROGRESS' ? '😄' :
          gameStatus === 'LOST' ? '💣' : '😎'}
      </button>
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
                  cell.state === 'REVEALED' && cell.adjacentMines > 0 ? (
                    <span style={{ color: getColor(cell.adjacentMines) }}>
                      {cell.adjacentMines}
                    </span>
                  ) : cell.state === 'FLAGGED' ? '🚩' : ''}
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
