const stompClient = new StompJs.Client({
    brokerURL: 'ws://localhost:8080/my-websocket'
});

stompClient.onConnect = function () {
    console.log("✅ Conectado al WebSocket");

    // 📌 SUSCRIPCIÓN PARA RECIBIR LA CONFIGURACIÓN CORRECTA
    stompClient.subscribe("/topic/game-config", function (message) {
        const config = JSON.parse(message.body);
        console.log("📩 Configuración recibida:", config);

        // Actualiza la UI con los valores correctos
        document.getElementById("timer").textContent = `Tiempo: ${config.duracion} min`;
        document.getElementById("playerLives").textContent = `Vidas: ${config.vidas}`;

        // ⚡ Actualizar el temporizador con la configuración correcta
        startGame(config);

        // ⚡ Cargar el mapa después de recibir la configuración correcta
        loadGameMap();
    });
};

// Activa la conexión STOMP
stompClient.activate();

// 🕒 Función para iniciar el temporizador correctamente
function startGame(config) {
    let timeLeft = config.duracion * 60;
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
}

// 🗺️ Función para cargar el mapa
function loadGameMap() {
    fetch('http://localhost:8080/game/map')
        .then(response => response.text()) // Recibimos el mapa como texto
        .then(mapString => {
            console.log("📌 Mapa recibido:\n" + mapString);
            drawMap(mapString);
        })
        .catch(error => console.error("❌ Error cargando el mapa:", error));
}

// 🎨 Función para dibujar el mapa en el canvas
function drawMap(mapString) {
    console.log("📌 Dibujando mapa...");
    const canvas = document.getElementById("gameCanvas");

    if (!canvas) {
        console.error("❌ Error: No se encontró el canvas.");
        return;
    }

    const ctx = canvas.getContext("2d");
    const cellSize = 40;
    const rows = mapString.trim().split("\n");
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
    console.log("✅ Mapa dibujado en canvas.");

}
