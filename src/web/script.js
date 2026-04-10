// Render ka backend URL (Ise apne actual URL se confirm kar lein)
const API_BASE = "https://smart-waste-management-coordinator1.onrender.com";

// Login page se user ka naam uthayein (Default: Guest)
const currentUser = localStorage.getItem("currentUser") || "Guest";

// 1. Bins ka data fetch karne ka function
async function fetchBins() {
    try {
        const res = await fetch(`${API_BASE}/api/bins`);
        if (!res.ok) throw new Error("Network response was not ok");
        
        const data = await res.text();
        const bins = data.split(";");
        const tableBody = document.getElementById("binTableBody");
        
        if (!tableBody) return; // Agar table element nahi mila
        
        tableBody.innerHTML = ""; // Purana data saaf karein

        bins.forEach(line => {
            if (!line.trim()) return;

            // Data Structure: ID, Location, Level, Status, AssignedDriver
            const [id, loc, level, status, assignedDriver] = line.split(",");
            
            let actionHTML = "";

            if (status === "1") { 
                // CASE 1: Bin Full hai (Status 1)
                actionHTML = `<button class="btn-transit" onclick="startTransit('${id}')">I am going</button>`;
            } 
            else if (status === "2") { 
                // CASE 2: Bin Transit mein hai (Status 2)
                if (assignedDriver === currentUser) {
                    // Agar isi user ne pick kiya hai toh "Mark Done" dikhao
                    actionHTML = `<button class="btn-done" onclick="markDone('${id}')">Mark Done</button>`;
                } else {
                    // Agar kisi aur ne pick kiya hai toh "Processing..." dikhao
                    actionHTML = `<span class="badge-processing">Processing...</span>`;
                }
            } 
            else { 
                // CASE 3: Bin Empty hai (Status 0)
                actionHTML = `<span class="badge-empty">Cleaned</span>`;
            }

            // Table mein row add karein
            tableBody.innerHTML += `
                <tr>
                    <td><strong>#${id}</strong></td>
                    <td>${loc}</td>
                    <td>
                        <div style="display:flex; align-items:center; gap:10px;">
                            <div style="width:100px; background:#eee; border-radius:5px; overflow:hidden; height:10px;">
                                <div style="width:${level}%; background:${level > 80 ? '#d32f2f' : '#2e7d32'}; height:100%;"></div>
                            </div>
                            <span>${level}%</span>
                        </div>
                    </td>
                    <td>${actionHTML}</td>
                </tr>`;
        });
    } catch (error) {
        console.error("Error fetching bins:", error);
    }
}

// 2. "I am going" click karne par
async function startTransit(id) {
    try {
        // ID aur Current User dono bhej rahe hain taaki lock lag sake
        const response = await fetch(`${API_BASE}/api/transit`, {
            method: "POST",
            body: `${id},${currentUser}`
        });
        
        if (response.ok) {
            fetchBins(); // Table update karein
        }
    } catch (error) {
        alert("Failed to update status. Please try again.");
    }
}

// 3. "Mark Done" click karne par (Reset status)
async function markDone(id) {
    try {
        const response = await fetch(`${API_BASE}/api/reset`, {
            method: "POST",
            body: id
        });
        
        if (response.ok) {
            fetchBins(); // Table update karein
        }
    } catch (error) {
        alert("Failed to reset bin. Please try again.");
    }
}

// 4. Logout function (Optional)
function logout() {
    localStorage.removeItem("isLoggedIn");
    localStorage.removeItem("currentUser");
    window.location.href = "login.html";
}

// Har 5 second mein auto-refresh (Real-time update ke liye)
setInterval(fetchBins, 5000);

// Page load hote hi data fetch karein
window.onload = fetchBins;
