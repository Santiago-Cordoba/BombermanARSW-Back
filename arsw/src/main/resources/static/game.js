// Obtener parámetros de la URL
const params = new URLSearchParams(window.location.search);
const duration = params.get("duration") || "5";  // Si es null, usa 5 minutos
const players = params.get("players") || "2";   // Si es null, usa 2 jugadores
const blocks = params.get("blocks") || "10";    // Si es null, usa 10 bloques
const lives = params.get("lives") || "3";       // Si es null, usa 3 vidas

// Mostrar información en pantalla
document.getElementById("timer").textContent = `Tiempo: ${duration} min`;
document.getElementById("playerLives").textContent = `Vidas: ${lives}`;

// Simulación de inicio del juego
console.log("Iniciando juego con configuración:");
console.log("Duración:", duration);
console.log("Jugadores:", players);
console.log("Bloques:", blocks);
console.log("Vidas:", lives);


// Simulación de un temporizador
let timeLeft = duration * 60;
const timerElement = document.getElementById("timer");

function updateTimer() {
    let minutes = Math.floor(timeLeft / 60);
    let seconds = timeLeft % 60;
    timerElement.textContent = `Tiempo: ${minutes}:${seconds < 10 ? "0" : ""}${seconds}`;

    if (timeLeft > 0) {
        timeLeft--;
        setTimeout(updateTimer, 1000);
    } else {
        alert("¡Tiempo terminado!");
    }
}

updateTimer();

function loadGameMap() {
    fetch('http://localhost:8080/game/map')
        .then(response => response.text()) // Recibimos el mapa como texto
        .then(mapString => {
            console.log("Mapa recibido:\n" + mapString);
            drawMap(mapString);
        })
        .catch(error => console.error("Error cargando el mapa:", error));
}

function drawMap(mapString) {
    const canvas = document.getElementById("gameCanvas");
    const ctx = canvas.getContext("2d");

    const cellSize = 40;
    const rows = mapString.trim().split("\n"); // Dividir el texto en filas
    const cols = rows[0].length;

    canvas.width = cols * cellSize;
    canvas.height = rows.length * cellSize;

    for (let y = 0; y < rows.length; y++) {
        for (let x = 0; x < cols; x++) {
            if (rows[y][x] === "#") {
                ctx.fillStyle = "black"; // Pared
            } else if (rows[y][x] === "P") {
                ctx.fillStyle = "red"; // Jugador
            } else {
                ctx.fillStyle = "white"; // Espacio vacío
            }
            ctx.fillRect(x * cellSize, y * cellSize, cellSize, cellSize);
        }
    }
}

// Cargar el mapa cuando se abra `game.html`
window.onload = loadGameMap;
