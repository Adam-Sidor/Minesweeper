import React, { useEffect, useState, useCallback } from 'react';
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

  const startGame = useCallback(async () => {
    try {
      const res = await axios.post('http://localhost:8080/api/game/start', gameConfig);
      setBoard(res.data.board);
      setGameStatus('IN_PROGRESS');
      setRemainingMines(res.data.remainingMines);
      setTime(0);
      setHasStarted(false);
    } catch (error) {
      console.error("Error starting the game:", error);
    }
  }, [gameConfig]);

  const revealCell = async (row: number, col: number) => {
    try {
      const res = await axios.post('http://localhost:8080/api/game/reveal', { row, col });
      setBoard(res.data.board);
      setGameStatus(res.data.status);
      if (!hasStarted) {
        setHasStarted(true);
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
  }, []);

  useEffect(() => {
    startGame();
  }, [gameConfig, startGame]);

  const [hasStarted, setHasStarted] = useState(false);

  useEffect(() => {
    let interval: NodeJS.Timeout | null = null;

    if (hasStarted && gameStatus === 'IN_PROGRESS') {
      interval = setInterval(() => {
        setTime(prevTime => prevTime + 1);
      }, 1000);
    }
    return () => {
      if (interval) clearInterval(interval);
    };
  }, [hasStarted, gameStatus]);
  return (
    <div className='app-wrapper'>
      <div className="App">
        <h1>Minesweeper</h1>
        <nav className='top-bar'>
          <div className='nav-left'>
            Mines left: <br />
            {remainingMines}
          </div>
          <div className='nav-center'>
            <button onClick={() => setShowDifficultyMenu(!showDifficultyMenu)}>Poziom trudności</button>
            {showDifficultyMenu &&
              <div>
                <button className='difficultyButton' onClick={() => setDifficulty(9, 9, 10)}>Łatwy</button>
                <button className='difficultyButton' onClick={() => setDifficulty(16, 16, 40)}>Średni</button>
                <button className='difficultyButton' onClick={() => setDifficulty(16, 30, 99)}>Trudny</button>
                <button className='difficultyButton' onClick={() => setShowCustomDifficultyMenu(true)}>Własny</button>
                {showCustomDifficultyMenu && (
                  <div className='custom-difficulty-form'>
                    {errorMessage && <div className='error-message'>{errorMessage}</div>}
                    <div className="form-row">
                      <label>Wiersze</label>
                      <input type="number" min='2' max='50' onChange={e => setCustomRows(+e.target.value)} />
                    </div>
                    <div className="form-row">
                      <label>Kolumny</label>
                      <input type="number" min='2' max='50' onChange={e => setCustomCols(+e.target.value)} />
                    </div>
                    <div className="form-row">
                      <label>Miny</label>
                      <input type="number" min='1' onChange={e => setCustomMines(+e.target.value)} />
                    </div>
                    <div className="form-footer">
                      <button onClick={applyCustomDifficulty}>Zatwierdź</button>
                    </div>
                  </div>
                )}

              </div>}
            <button onClick={startGame}>
              {gameStatus === 'IN_PROGRESS' ? '😄' :
                gameStatus === 'LOST' ? '💣' : '😎'}
            </button>
          </div>
          <div className='nav-right'>
            Time: <br />
            {time} sec
          </div>
        </nav>
        <div className='game-over-banner'>
          {gameStatus === 'WON' && <div className="status-banner success">🎉 Gratulacje, wygrałeś!</div>}
          {gameStatus === 'LOST' && <div className="status-banner fail">💥 Przegrałeś! Spróbuj ponownie.</div>}
        </div>
        <div className='board-container'>
          {board.map((row, r) => (
            <div key={r} className='board-row'>
              {row.map((cell, c) => (
                <button
                  key={c}
                  onClick={() => revealCell(r, c)}
                  onContextMenu={(e) => {
                    e.preventDefault();
                    flagCell(r, c);
                  }}
                  disabled={gameStatus !== 'IN_PROGRESS'}
                  className={`cell-button ${cell.state === 'REVEALED' ? 'cell-revealed' : ''} ${cell.state === 'REVEALED' && cell.hasMine ? 'has-mine' : ''} ${cell.state === 'REVEALED' && cell.adjacentMines ? `mine-${cell.adjacentMines}` : ''}`}
                >
                  {cell.hasMine && gameStatus === 'LOST' && cell.state === 'REVEALED' ? '💥' :
                    cell.hasMine && gameStatus === 'LOST' && cell.state === 'FLAGGED' ? '💣' :
                      cell.state === 'REVEALED' && cell.adjacentMines > 0 ? (
                        <span className={`mine-count mine-${cell.adjacentMines}`}>
                          {cell.adjacentMines}
                        </span>

                      ) : cell.state === 'FLAGGED' ? '🚩' : ''}
                </button>
              ))}
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}

export default App;
