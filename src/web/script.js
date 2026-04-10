const API_BASE = "https://smart-waste-management-coordinator1.onrender.com";
const currentUser = localStorage.getItem("currentUser") || "Guest";

// 1. Dashboard par Bins load karne ka function
// async function fetchBins() {
//     try {
//         const res = await fetch(`${API_BASE}/api/bins`);
//         const data = await res.text();
//         const bins = data.split(";");
//         const tableBody = document.getElementById("binTableBody");
        
//         if (!tableBody) return;
//         tableBody.innerHTML = "";

//         bins.forEach(line => {
//             if (!line.trim()) return;
//             const [id, loc, level, status, assignedDriver] = line.split(",");
            
//             let actionHTML = "";
//             if (status === "1") { 
//                 actionHTML = `<button class="btn-transit" onclick="startTransit('${id}')">I am going</button>`;
//             } else if (status === "2") { 
//                 if (assignedDriver === currentUser) {
//                     actionHTML = `<button class="btn-done" onclick="markDone('${id}')">Mark Done</button>`;
//                 } else {
//                     actionHTML = `<span class="badge-processing">Processing...</span>`;
//                 }
//             } else { 
//                 actionHTML = `<span class="badge-empty">Cleaned</span>`;
//             }

//             tableBody.innerHTML += `
//                 <tr>
//                     <td><strong>#${id}</strong></td>
//                     <td>${loc}</td>
//                     <td>
//                         <div style="display:flex; align-items:center; gap:10px;">
//                             <div style="width:100px; background:#eee; border-radius:5px; height:10px; overflow:hidden;">
//                                 <div style="width:${level}%; background:${level > 80 ? '#d32f2f' : '#2e7d32'}; height:100%;"></div>
//                             </div>
//                             <span>${level}%</span>
//                         </div>
//                     </td>
//                     <td>${actionHTML}</td>
//                 </tr>`;
//         });
//     } catch (err) {
//         console.error("Fetch Error:", err);
//     }
// }

async function fetchBins() {
    try {
        const res = await fetch(`${API_BASE}/api/bins`);
        const data = await res.text();
        
        const tableBody = document.getElementById("binTableBody");
        if (!tableBody || !data.trim()) {
            // Agar data khali hai toh empty message dikhayein
            tableBody.innerHTML = "<tr><td colspan='4'>No Dustbins Found</td></tr>";
            return;
        }

        const bins = data.split(";");
        tableBody.innerHTML = ""; // Purana data saaf karein

        bins.forEach(line => {
            if (!line.trim()) return;
            const parts = line.split(",");
            
            // Backend format: ID, Loc, Level, Status, Driver
            const [id, loc, level, status, driver] = parts;

            tableBody.innerHTML += `
                <tr>
                    <td>${id}</td>
                    <td>${loc}</td>
                    <td>${level}%</td>
                    <td>${status === "2" ? "Processing..." : "Active"}</td>
                </tr>`;
        });
    } catch (err) {
        console.error("Fetch Error:", err);
    }
}

// 2. NAYA BIN ADD KARNE KA FUNCTION (Missing Code)
async function addBin() {
    // Screenshot ke hisaab se inputs ki values nikaal rahe hain
    const idInput = document.querySelectorAll('input')[0]; // Pehla input: Bin ID
    const locInput = document.querySelectorAll('input')[1]; // Dusra input: Location
    const levelInput = document.querySelectorAll('input')[2]; // Tisra input: Level

    const id = idInput.value;
    const loc = locInput.value;
    const level = levelInput.value;

    if(!id || !loc || !level) {
        alert("Saari details bhariye!");
        return;
    }

    // Backend (WebServer.java) Status 0 aur Driver 'None' khud jodd lega
    const data = `${id},${loc},${level}`;

    try {
        const response = await fetch(`${API_BASE}/api/add`, {
            method: "POST",
            body: data
        });

        if (response.ok) {
            alert("Naya Dustbin Add Ho Gaya!");
            // Inputs clear karein
            idInput.value = "";
            locInput.value = "";
            levelInput.value = "";
            fetchBins(); // Table refresh karein
        } else {
            alert("Server error! Bin add nahi ho paya.");
        }
    } catch (error) {
        console.error("Error:", error);
        alert("Connection error!");
    }
}

// 3. Status update functions
async function startTransit(id) {
    await fetch(`${API_BASE}/api/transit`, {
        method: "POST",
        body: `${id},${currentUser}`
    });
    fetchBins();
}

async function markDone(id) {
    await fetch(`${API_BASE}/api/reset`, {
        method: "POST",
        body: id
    });
    fetchBins();
}

// Auto-refresh and Init
setInterval(fetchBins, 5000);
window.onload = fetchBins;
