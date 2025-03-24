const stompClient = new StompJs.Client({
    brokerURL: 'ws://localhost:8080/my-websocket'
});

stompClient.onConnect = function () {
    console.log("✅ Conectado al WebSocket");

    // Suscribirse al tópico de configuración
    stompClient.subscribe("/topic/game-config", function (message) {
        const config = JSON.parse(message.body);
        alert(`🎮 Configuración Recibida: 
        ⏳ Duración: ${config.duration} min
        👥 Jugadores: ${config.players} 
        🧱 Bloques: ${config.blocks} 
        ❤️ Vidas: ${config.lives}`);
    });
};

// Activa la conexión STOMP
stompClient.activate();

function sendConfig() {
    const duration = document.getElementById("duration").value;
    const players = document.getElementById("players").value;
    const blocks = document.getElementById("blocks").value;
    const lives = document.getElementById("lives").value;

    const config = {
        duration: duration,
        playerCount: players,
        blockCount: blocks,
        lives: lives
    };

    fetch('http://localhost:8080/game/start', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(config)
    })
        .then(response => {
            if (!response.ok) {
                return response.text().then(text => { throw new Error(text); });
            }
            return response.text();
        })
        .then(data => {
            console.log("Juego iniciado:", data);
            // 🔥 Redirigir a game.html con los parámetros de configuración
            window.location.href = `game.html?duration=${duration}&players=${players}&blocks=${blocks}&lives=${lives}`;
        })
        .catch(error => {
            console.error("Error iniciando el juego:", error);
        });
}

