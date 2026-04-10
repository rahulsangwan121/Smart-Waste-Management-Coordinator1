async function loadDustbins() {
    try {
        const response = await fetch('http://localhost:8080/api/bins');
        const data = await response.text();
        const binListDiv = document.getElementById('bin-list');

        if (!data.trim()) {
            binListDiv.innerHTML = "<p>No bins available.</p>";
            return;
        }

        let html = "<table><tr><th>Bin ID</th><th>Location</th><th>Level</th><th>Status</th><th>Actions</th></tr>";
        const bins = data.split(';');

        bins.forEach(bin => {
            const parts = bin.split(',');
            if (parts.length >= 3) {
                const level = parseInt(parts[2]);
                const statusFlag = parts[3] || "1";

                let statusText, actionBtn, rowClass = "normal";

                if (level < 80) {
                    statusText = "✅ OK";
                    actionBtn = "---";
                } else if (level >= 80 && statusFlag === "1") {
                    statusText = "🚨 FULL - Needs Pickup";
                    rowClass = "critical";
                    actionBtn = `<button class="go-btn" onclick="setTransit('${parts[0]}')">🏃 I am Going</button>`;
                } else if (statusFlag === "2") {
                    statusText = "🚚 Driver is Coming...";
                    rowClass = "transit";
                    actionBtn = `<button class="done-btn" onclick="resetBin('${parts[0]}')">✅ Mark Done</button>`;
                }

                html += `<tr class="${rowClass}">
                            <td>${parts[0]}</td><td>${parts[1]}</td><td>${level}%</td>
                            <td>${statusText}</td><td>${actionBtn}</td>
                         </tr>`;
            }
        });
        binListDiv.innerHTML = html + "</table>";
    } catch (e) { console.log("Server not reachable"); }
}

async function setTransit(id) {
    await fetch('http://localhost:8080/api/transit', { method: 'POST', body: id });
    loadDustbins();
}

async function resetBin(id) {
    if(confirm("Confirm Pickup?")) {
        await fetch('http://localhost:8080/api/reset', { method: 'POST', body: id });
        loadDustbins();
    }
}

async function addBin() {
    const id = document.getElementById('binID').value;
    const loc = document.getElementById('location').value;
    const level = document.getElementById('fillLevel').value;
    if(level < 0 || level > 100) return alert("Invalid level");

    await fetch('http://localhost:8080/api/add', { method: 'POST', body: `${id},${loc},${level},1` });
    loadDustbins();
}

loadDustbins();
setInterval(loadDustbins, 5000);