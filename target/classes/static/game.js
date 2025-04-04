// Obtener parámetros de la URL
const params = new URLSearchParams(window.location.search);
const duration = params.get("duration") || "5";
const players = params.get("players") || "2";
const blocks = params.get("blocks") || "10";
const lives = params.get("lives") || "3";

// Mostrar información en pantalla
document.getElementById("timer").textContent = `Tiempo: ${duration} min`;
document.getElementById("playerLives").textContent = `Vidas: ${lives}`;

console.log("Iniciando juego con configuración:");
console.log("Duración:", duration);
console.log("Jugadores:", players);
console.log("Bloques:", blocks);
console.log("Vidas:", lives);

// Escuchar las teclas y enviar movimiento al backend
// Escuchar las teclas y enviar movimiento al backend
document.addEventListener("keydown", (event) => {
    let direction = null;
    let action = null;

    if (event.key === "ArrowUp") direction = "UP";
    if (event.key === "ArrowDown") direction = "DOWN";
    if (event.key === "ArrowLeft") direction = "LEFT";
    if (event.key === "ArrowRight") direction = "RIGHT";
    if (event.key === "f" || event.key === "F") action = "PLACE_BOMB";

    if (direction) {
        fetch(`http://localhost:8080/game/move?direction=${direction}`, { method: "POST" })
            .then(response => response.text())
            .then(updatedMap => {
                console.log("Mapa actualizado desde el servidor:\n", updatedMap);
                drawMap(updatedMap);
            })
            .catch(error => console.error("Error al mover el jugador:", error));
    }

    if (action === "PLACE_BOMB") {
        fetch(`http://localhost:8080/game/placeBomb`, { method: "POST" })
            .then(response => response.text())
            .then(updatedMap => {
                console.log("Bomba colocada. Mapa actualizado:\n", updatedMap);
                drawMap(updatedMap);
            })
            .catch(error => console.error("Error al colocar bomba:", error));
    }
});

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

// Función para cargar el mapa inicial desde el backend
function loadGameMap() {
    fetch('http://localhost:8080/game/map')
        .then(response => response.text())
        .then(mapString => {
            console.log("Mapa recibido:\n" + mapString);
            drawMap(mapString);
        })
        .catch(error => console.error("Error cargando el mapa:", error));
}

// Función para dibujar el mapa en el canvas
function drawMap(mapString) {
    const canvas = document.getElementById("gameCanvas");
    const ctx = canvas.getContext("2d");

    const cellSize = 40;
    let rows = mapString.trim().split("\n").map(row => row.split(""));
    const cols = rows[0].length;

    canvas.width = cols * cellSize;
    canvas.height = rows.length * cellSize;

    // Hacer el fondo del canvas transparente
    canvas.style.backgroundColor = "transparent";

    const wallImage = new Image();
    wallImage.src = "/img/muro.png";
    const playerImage = new Image();
    playerImage.src = "/img/player.png";

    wallImage.onload = playerImage.onload = function () {
        ctx.clearRect(0, 0, canvas.width, canvas.height);

        for (let y = 0; y < rows.length; y++) {
            for (let x = 0; x < cols; x++) {
                if (rows[y][x] === "#") {
                    ctx.drawImage(wallImage, x * cellSize, y * cellSize, cellSize, cellSize);
                } else if (rows[y][x] === "P") {
                    ctx.drawImage(playerImage, x * cellSize, y * cellSize, cellSize, cellSize);
                }

                // Dibujar líneas de la cuadrícula
                ctx.strokeStyle = "rgba(0, 0, 0, 0.3)"; // Color de las líneas
                ctx.strokeRect(x * cellSize, y * cellSize, cellSize, cellSize);
            }
        }
    };





}

// Cargar el mapa cuando se abra `game.html`
window.onload = loadGameMap;
