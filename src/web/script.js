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

            const assignedUser = parts[4] || "";
            const currentUser = localStorage.getItem("currentUser");

            if (parts.length >= 3 && parts[0]) {
                const level = parseInt(parts[2]);
                const statusFlag = parts[3] || "1";

                let statusText = "";
                let actionBtn = "";
                let rowClass = "normal";

                if (level < 80) {
                    statusText = "✅ OK";
                    actionBtn = "---";
                }
                else if (level >= 80 && statusFlag === "1") {
                    statusText = "FULL - Needs Pickup";
                    rowClass = "critical";
                    actionBtn = `<button class="go-btn" onclick="setTransit('${parts[0]}')">Reach</button>`;
                }
                else if (statusFlag === "2") {
                    rowClass = "transit";

                    if (assignedUser === currentUser) {
                        statusText = "In Progress...";
                        actionBtn = `<button class="done-btn" onclick="resetBin('${parts[0]}')">Mark Done</button>`;
                    } else {
                        statusText = `In Progress..`;
                        actionBtn = `Assigned`;
                    }
                }

                html += `<tr class="${rowClass}">
                    <td>${parts[0]}</td>
                    <td>${parts[1]}</td>
                    <td>${level}%</td>
                    <td>${statusText}</td>
                    <td>${actionBtn}</td>
                </tr>`;
            }
        });

        binListDiv.innerHTML = html + "</table>";
    } catch (e) {
        console.log("Server not reachable at: " + API_BASE);
        document.getElementById('bin-list').innerHTML = "<p>⚠️ Server not reachable.</p>";
    }
}

async function setTransit(id) {
    const user = localStorage.getItem("currentUser");

    if (!user) {
        alert("Please login first.");
        window.location.href = "login.html";
        return;
    }

    await fetch(`${API_BASE}/api/transit`, {
        method: 'POST',
        headers: {
            "Content-Type": "text/plain"
        },
        body: `${id},${user}`
    });

    loadDustbins();
}

async function resetBin(id) {
    const user = localStorage.getItem("currentUser");

    if (!user) {
        alert("Please login first.");
        window.location.href = "login.html";
        return;
    }

    if (confirm("Confirm Pickup?")) {
        const res = await fetch(`${API_BASE}/api/reset`, {
            method: 'POST',
            headers: {
                "Content-Type": "text/plain"
            },
            body: `${id},${user}`
        });

        const result = await res.text();

        if (result === "NOT_ALLOWED") {
            alert("❌ Only assigned driver can complete this task!");
            return;
        }

        loadDustbins();
    }
}

async function addBin() {
    const id = document.getElementById('binID').value.trim();
    const loc = document.getElementById('location').value.trim();
    const level = document.getElementById('fillLevel').value.trim();

    if (!id || !loc || level === "") {
        return alert("All fields are required!");
    }

    if (level < 0 || level > 100) {
        return alert("Invalid level");
    }

    await fetch(`${API_BASE}/api/add`, {
        method: 'POST',
        headers: {
            "Content-Type": "text/plain"
        },
        body: `${id},${loc},${level},1,`
    });

    document.getElementById('binID').value = "";
    document.getElementById('location').value = "";
    document.getElementById('fillLevel').value = "";

    loadDustbins();
}

loadDustbins();
setInterval(loadDustbins, 5000);
