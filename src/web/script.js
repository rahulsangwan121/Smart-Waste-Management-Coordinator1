const API_BASE = "https://smart-waste-management-coordinator1.onrender.com";

async function loadDustbins() {
    try {
        const response = await fetch(`${API_BASE}/api/bins`);
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
            if (parts.length >= 3 && parts[0]) {

                const level = parseInt(parts[2]);
                const statusFlag = parts[3] || "1";

                let statusText, actionBtn, rowClass = "normal";

                if (level < 80) {
                    statusText = "✅ OK";
                    actionBtn = "---";
                } else if (level >= 80 && statusFlag === "1") {
                    statusText = "FULL - Needs Pickup";
                    rowClass = "critical";
                    actionBtn = `<button class="go-btn" onclick="setTransit('${parts[0]}')">Reach to bin</button>`;
                } else if (statusFlag === "2") {
                    statusText = "In Progress...";
                    rowClass = "transit";
                    actionBtn = `<button class="done-btn" onclick="resetBin('${parts[0]}')">✅ Mark Done</button>`;
                }

                html += `<tr class="${rowClass}">
                    <td>${parts[0]}</td>
                    <td>${parts[1]}</td>
                    <td>${level}%</td>
                    <td>${statusText}</td>
                    <td>
                        ${actionBtn}
                        <button class="delete-btn" onclick="deleteBin('${parts[0]}')">🗑️ Delete</button>
                    </td>
                </tr>`;
            }
        });

        binListDiv.innerHTML = html + "</table>";
    } catch (e) {
        console.log("Server not reachable");
    }
}

async function setTransit(id) {
    await fetch(`${API_BASE}/api/transit`, { method: 'POST', body: id });
    loadDustbins();
}

async function resetBin(id) {
    if(confirm("Confirm Pickup?")) {
        await fetch(`${API_BASE}/api/reset`, { method: 'POST', body: id });
        loadDustbins();
    }
}

async function deleteBin(id) {
    if(confirm("Delete this bin?")) {
        await fetch(`${API_BASE}/api/delete`, { method: 'POST', body: id });
        loadDustbins();
    }
}

async function addBin() {
    const id = document.getElementById('binID').value;
    const loc = document.getElementById('location').value;
    const level = document.getElementById('fillLevel').value;

    if (!id || !loc || level === "") {
        return alert("All fields required!");
    }

    if(level < 0 || level > 100) {
        return alert("Invalid level");
    }

    await fetch(`${API_BASE}/api/add`, { method: 'POST', body: `${id},${loc},${level},1` });
    loadDustbins();
}

loadDustbins();
setInterval(loadDustbins, 5000);
