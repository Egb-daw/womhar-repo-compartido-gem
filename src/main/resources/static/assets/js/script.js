/**
 * DCIM - Gestión de Racks y Equipamiento
 */
let selectedElement = null;
let selectedIndex = -1;
let gridData = [];
const DEFAULT_COLOR = '#4CAF50';
const DEFAULT_NAME_PREFIX = 'Rack ';

// --- INICIALIZACIÓN ---
function generarCuadricula() {
    const container = document.getElementById('gridContainer');
    const widthInput = document.getElementById('widthInput');
    const heightInput = document.getElementById('heightInput');

    const width = parseInt(widthInput.value);
    const height = parseInt(heightInput.value);

    if (isNaN(width) || isNaN(height) || width < 1 || height < 1) {
        alert("Ingresa valores válidos para las dimensiones.");
        return;
    }

    container.innerHTML = '';
    selectedElement = null;
    selectedIndex = -1;

    document.getElementById('emptyState').style.display = 'block';
    document.getElementById('actionForm').style.display = 'none';

    container.style.gridTemplateColumns = `repeat(${width}, 1fr)`;
    const totalItems = width * height;

    // Intentar cargar datos de localStorage
    const savedData = localStorage.getItem('dcim_grid_data');
    gridData = [];

    if (savedData) {
        try {
            const savedGrid = JSON.parse(savedData);
            if (savedGrid.length === totalItems) {
                gridData = savedGrid;
            }
        } catch (e) { console.error("Error cargando caché:", e); }
    }

    if (gridData.length === 0) {
        for (let i = 0; i < totalItems; i++) {
            gridData.push({
                id: null, name: '', desc: '', capacity: 42,
                color: DEFAULT_COLOR, isEmpty: true, equipment: []
            });
        }
    }

    // Renderizar visualmente
    gridData.forEach((data, i) => {
        const div = document.createElement('div');
        div.className = data.isEmpty ? 'grid-item empty' : 'grid-item';
        div.dataset.index = i;
        div.textContent = data.name || 'Vacío';
        if (!data.isEmpty) div.style.backgroundColor = data.color;
        div.addEventListener('click', () => seleccionarCelda(div, i));
        container.appendChild(div);
    });
}

// --- PERSISTENCIA ---
function guardarDatos() {
    localStorage.setItem('dcim_grid_data', JSON.stringify(gridData));
}

// --- GESTIÓN DE SELECCIÓN ---
function seleccionarCelda(element, index) {
    if (selectedElement) selectedElement.classList.remove('selected');
    selectedElement = element;
    selectedIndex = index;
    selectedElement.classList.add('selected');

    const data = gridData[index];

    document.getElementById('editName').value = data.name || '';
    document.getElementById('editDesc').value = data.desc || '';
    document.getElementById('editCapacity').value = data.capacity;
    document.getElementById('editColor').value = data.color;

    document.getElementById('emptyState').style.display = 'none';
    document.getElementById('actionForm').style.display = 'block';

    configurarBotones(data.isEmpty);
    renderEquipmentGrid(data);
}

function configurarBotones(isEmpty) {
    const container = document.getElementById('buttonContainer');
    const title = document.getElementById('formTitle');
    container.innerHTML = '';

    if (isEmpty) {
        title.textContent = 'Nueva Celda / Rack';
        const btnCreate = document.createElement('button');
        btnCreate.className = 'btn btn-primary';
        btnCreate.textContent = 'Crear Rack';
        btnCreate.onclick = guardarCelda;
        container.appendChild(btnCreate);
    } else {
        title.textContent = `Editando: ${gridData[selectedIndex].name}`;
        const btnSave = document.createElement('button');
        btnSave.className = 'btn btn-primary';
        btnSave.textContent = 'Actualizar';
        btnSave.onclick = guardarCelda;
        container.appendChild(btnSave);

        const btnDelete = document.createElement('button');
        btnDelete.className = 'btn btn-delete';
        btnDelete.textContent = 'Borrar';
        btnDelete.onclick = borrarCeldaLocal;
        container.appendChild(btnDelete);
    }
}

// --- OPERACIONES RACK ---
async function guardarCelda() {
    const nameInput = document.getElementById('editName').value.trim();
    if (!nameInput) { alert("El nombre del Rack es obligatorio"); return; }

    const width = parseInt(document.getElementById('widthInput').value);

    // 1. Construimos el objeto EXACTO que tu RackDTO espera recibir
    const rackDTO = {
        id: gridData[selectedIndex].id || null,
        locationLabel: nameInput,
        description: document.getElementById('editDesc').value,
        capacityU: parseInt(document.getElementById('editCapacity').value),
        positionX: selectedIndex % width,
        positionY: Math.floor(selectedIndex / width),

        // --- CAMPOS OBLIGATORIOS SEGÚN TUS LOGS ---
        catalogStock: 0,          // Valor por defecto para que no sea null
        catalogPrice: 0.0,        // Valor por defecto
        catalogVisible: true,     // Valor por defecto
        status: "ACTIVE",         // Estado inicial
        roomId: 1,                // ¡OJO! Aquí deberías poner un ID de sala válido que exista en tu BD

        equipments: gridData[selectedIndex].equipment.map(eq => ({
            id: eq.id || null,
            name: eq.name,
            slotPositionU: eq.topU,
            slotHeightU: eq.height,
            description: eq.desc,
            functionality: eq.func,
            componentType: eq.comp || "SERVER"
        }))
    };


    // 2. Envío al controlador MapcpdController
    try {
        const response = await fetch('/api/mapcpd/racks', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                // Si tienes activado Spring Security, esto es vital:
                'X-CSRF-TOKEN': document.querySelector('meta[name="_csrf"]')?.content
            },
            body: JSON.stringify(rackDTO)
        });

        if (response.ok) {
            const savedData = await response.json();
            // Guardamos el ID que nos devuelve la base de datos
            gridData[selectedIndex].id = savedData.id;

            statusMsg("¡Conexión exitosa! Guardado en MariaDB");
            actualizarInterfazTrasGuardar(savedData);
        } else {
            const errorText = await response.text();
            console.error("Error del servidor:", errorText);
            alert("El servidor rechazó los datos. Revisa la consola.");
        }
    } catch (error) {
        console.error("Error de red:", error);
        alert("No se pudo contactar con el servidor. ¿Está el backend corriendo?");
    }
}


async function cargarDatosDesdeBackend() {
    try {
        const response = await fetch('/api/mapcpd/racks');
        if (!response.ok) throw new Error("Error al obtener racks");

        const racksDesdeBD = await response.json();

        // Aquí deberías mapear los racks recibidos a tu array gridData
        // basado en sus posiciones X e Y.
        racksDesdeBD.forEach(rack => {
            const index = rack.positionX + (rack.positionY * parseInt(document.getElementById('widthInput').value));
            if (gridData[index]) {
                gridData[index] = {
                    id: rack.id,
                    name: rack.locationLabel,
                    capacity: rack.capacityU,
                    isEmpty: false,
                    equipment: rack.equipments.map(e => ({
                        id: e.id,
                        name: e.name,
                        topU: e.slotPositionU,
                        height: e.slotHeightU
                    }))
                };
            }
        });

        renderizarCuadriculaVisual(); // Refresca los colores y nombres
    } catch (error) {
        console.log("Trabajando en modo local (sin conexión a BD)");
    }
}


function borrarCeldaLocal() {
    if (!confirm("¿Borrar rack y todos sus equipos?")) return;

    gridData[selectedIndex] = {
        id: null, name: '', desc: '', capacity: 42,
        color: DEFAULT_COLOR, isEmpty: true, equipment: []
    };

    selectedElement.textContent = 'Vacío';
    selectedElement.style.backgroundColor = '';
    selectedElement.classList.add('empty');

    guardarDatos();
    resetFormUI();
    statusMsg("Rack eliminado");
}

// --- VISTA VERTICAL DE EQUIPOS ---
function renderEquipmentGrid(cellData) {
    const grid = document.getElementById('equipmentGrid');
    const section = document.getElementById('equipmentSection');
    const btnAdd = document.getElementById('btnAddEquipment');

    grid.innerHTML = '';
    if (cellData.isEmpty) {
        section.style.display = 'none';
        return;
    }

    section.style.display = 'block';
    document.getElementById('capacityDisplay').textContent = cellData.capacity;
    btnAdd.style.display = 'block';

    // Generar slots desde Capacity hasta 1 (Vista de arriba hacia abajo)
    for (let i = cellData.capacity - 1; i >= 0; i--) {
        const slot = document.createElement('div');
        slot.className = 'eq-slot';

        const eq = cellData.equipment.find(e => i >= e.topU && i < e.topU + e.height);

        if (eq) {
            slot.classList.add('occupied');
            slot.style.backgroundColor = cellData.color;
            if (i === eq.topU + eq.height - 1) {
                slot.innerHTML = `<span class="eq-slot-name">${eq.name}</span><span class="eq-slot-number">${i + 1}U</span>`;
            } else {
                slot.innerHTML = `<span class="eq-slot-number">${i + 1}</span>`;
            }
            slot.onclick = () => abrirModalEquipo(cellData.equipment.indexOf(eq));
        } else {
            slot.innerHTML = `<span class="eq-slot-number">${i + 1}</span>`;
            slot.onclick = () => abrirModalEquipo(null, i);
        }
        grid.appendChild(slot);
    }
}

// --- MODAL DE EQUIPOS ---
function abrirModalEquipo(index, suggestedTopU = null) {
    const modal = document.getElementById('equipmentModal');
    const btnContainer = document.getElementById('modalActions');
    const cellData = gridData[selectedIndex];

    btnContainer.innerHTML = '';
    modal.style.display = 'flex';

    const isEdit = index !== null;
    const equipment = isEdit ? cellData.equipment[index] : null;

    // Titulo y Valores
    document.getElementById('eqModalTitle').textContent = isEdit ? `Editar: ${equipment.name}` : "Nuevo Equipo";
    document.getElementById('eqName').value = isEdit ? equipment.name : "";
    document.getElementById('eqDesc').value = isEdit ? (equipment.desc || "") : "";
    document.getElementById('eqFunc').value = isEdit ? (equipment.func || "") : "";
    document.getElementById('eqComp').value = isEdit ? (equipment.comp || "") : "";

    // Altura (Asegurarnos que el input existe)
    let heightInput = document.getElementById('eqHeight');
    if (!heightInput) {
        const group = document.createElement('div');
        group.className = 'form-group';
        group.innerHTML = `<label>Altura (U):</label><input type="number" id="eqHeight" min="1" max="42">`;
        document.getElementById('eqComp').parentNode.insertBefore(group, document.getElementById('eqComp').nextSibling);
        heightInput = document.getElementById('eqHeight');
    }
    heightInput.value = isEdit ? equipment.height : 1;

    // BOTONES
    const btnCancelar = document.createElement('button');
    btnCancelar.className = 'btn btn-cancel';
    btnCancelar.textContent = 'Cancelar';
    btnCancelar.onclick = closeEquipmentModal;
    btnContainer.appendChild(btnCancelar);

    if (isEdit) {
        const btnDelete = document.createElement('button');
        btnDelete.className = 'btn btn-delete';
        btnDelete.textContent = 'Borrar';
        btnDelete.onclick = () => {
            if (confirm("¿Borrar equipo?")) {
                cellData.equipment.splice(index, 1);
                guardarYRefrescar();
            }
        };
        btnContainer.appendChild(btnDelete);
    }

    const btnSave = document.createElement('button');
    btnSave.className = 'btn btn-primary';
    btnSave.textContent = isEdit ? 'Guardar' : 'Añadir';
    btnSave.onclick = () => {
        const name = document.getElementById('eqName').value.trim();
        const height = parseInt(heightInput.value) || 1;
        if (!name) return alert("Nombre requerido");

        let targetTopU = isEdit ? equipment.topU : (suggestedTopU || 0);

        // Lógica de colisión
        let fits = true;
        for (let k = 0; k < height; k++) {
            if (targetTopU + k >= cellData.capacity ||
                cellData.equipment.some((e, idx) => idx !== index && (targetTopU + k >= e.topU && targetTopU + k < e.topU + e.height))) {
                fits = false; break;
            }
        }

        if (!fits) { // Buscar primer hueco si no cabe en el clickeado
            let found = false;
            for (let i = 0; i <= cellData.capacity - height; i++) {
                if (!cellData.equipment.some((e, idx) => idx !== index && Array.from({length: height}, (_, k) => i + k).some(u => u >= e.topU && u < e.topU + e.height))) {
                    targetTopU = i; found = true; break;
                }
            }
            if (!found) return alert("No hay espacio suficiente.");
        }

        const newEq = {
            name, height, topU: targetTopU,
            desc: document.getElementById('eqDesc').value,
            func: document.getElementById('eqFunc').value,
            comp: document.getElementById('eqComp').value
        };

        if (isEdit) cellData.equipment[index] = newEq;
        else cellData.equipment.push(newEq);

        guardarYRefrescar();
    };
    btnContainer.appendChild(btnSave);
}

function guardarYRefrescar() {
    guardarDatos();
    closeEquipmentModal();
    renderEquipmentGrid(gridData[selectedIndex]);
    statusMsg("Equipamiento actualizado");
}

function closeEquipmentModal() {
    document.getElementById('equipmentModal').style.display = 'none';
}

// --- UTILIDADES ---
function resetFormUI() {
    document.getElementById('emptyState').style.display = 'block';
    document.getElementById('actionForm').style.display = 'none';
    selectedIndex = -1;
    if (selectedElement) selectedElement.classList.remove('selected');
}

function statusMsg(msg) {
    const existing = document.querySelector('.status-msg');
    if (existing) existing.remove();
    const status = document.createElement('div');
    status.className = 'status-msg';
    status.innerHTML = `✓ ${msg}`;
    Object.assign(status.style, {
        position: 'fixed', bottom: '20px', right: '20px', backgroundColor: '#10b981',
        color: 'white', padding: '10px 20px', borderRadius: '5px', zIndex: '9999'
    });
    document.body.appendChild(status);
    setTimeout(() => status.remove(), 2500);
}
// --- CARGAR DATOS DESDE EL BACKEND ---
async function cargarDatosDesdeBD() {
    try {
        const response = await fetch('/api/mapcpd/racks');
        if (!response.ok) throw new Error("Error en la respuesta del servidor");

        const racksBD = await response.json();
        const width = parseInt(document.getElementById('widthInput').value) || 10;

        racksBD.forEach(rack => {
            // Calculamos la posición en el array basándonos en X e Y
            const index = (rack.positionY * width) + rack.positionX;

            if (index >= 0 && index < gridData.length) {
                gridData[index] = {
                    id: rack.id,
                    name: rack.locationLabel,
                    desc: rack.description || '',
                    capacity: rack.capacityU,
                    color: rack.color || '#4CAF50',
                    isEmpty: false,
                    equipment: rack.equipments ? rack.equipments.map(eq => ({
                        id: eq.id,
                        name: eq.name,
                        topU: eq.slotPositionU,
                        height: eq.slotHeightU,
                        desc: eq.description,
                        func: eq.functionality,
                        comp: eq.componentType
                    })) : []
                };
            }
        });

        // Esta función debe redibujar los cuadritos con los colores y nombres nuevos
        generarCuadriculaVisualmente();
    } catch (error) {
        console.error("Error al cargar desde BD:", error);
    }
}

// --- ACTUALIZAR SOLO LA VISTA ---
function generarCuadriculaVisualmente() {
    const container = document.getElementById('gridContainer');
    const items = container.querySelectorAll('.grid-item');

    gridData.forEach((data, i) => {
        const div = items[i];
        if (div && !data.isEmpty) {
            div.textContent = data.name;
            div.style.backgroundColor = data.color;
            div.classList.remove('empty');
        }
    });
}

// --- ACTUALIZAR EL LISTENER FINAL ---
// Asegúrate de que el final de tu archivo se vea así:
window.addEventListener('DOMContentLoaded', () => {
    generarCuadricula(); // Primero crea los divs vacíos
    cargarDatosDesdeBD(); // Luego trae los datos de Java y los rellena
});


// funcion final para ejecutar el script.js
window.addEventListener('DOMContentLoaded', () => {
    // 1. Inicializamos la cuadrícula vacía
    generarCuadricula();

    // 2. Cargamos los datos reales si existen
    // Importante: comprueba que la función está definida arriba
    if (typeof cargarDatosDesdeBD === 'function') {
        cargarDatosDesdeBD();
    }
});



