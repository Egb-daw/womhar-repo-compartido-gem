// script.js
let selectedElement = null;
let selectedIndex = -1;
let gridData = []; 
const DEFAULT_COLOR = '#4CAF50';
const DEFAULT_NAME_PREFIX = 'Celda ';

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
    // selectedElement y selectedIndex se reinician
    selectedElement = null;
    selectedIndex = -1;
    
    // Limpiar formulario
    document.getElementById('emptyState').style.display = 'block';
    document.getElementById('actionForm').style.display = 'none';
    
    const eqSection = document.querySelector('.equipment-section');
    if (eqSection) {
        eqSection.classList.remove('visible');
        document.getElementById('equipmentGrid').innerHTML = '';
    }

    container.style.gridTemplateColumns = `repeat(${width}, 1fr)`;

    const totalItems = width * height;
    gridData = []; // Reiniciamos el array

    // --- NUEVO: Cargar datos guardados si existen ---
    const savedData = localStorage.getItem('dcim_grid_data');
    let savedGrid = [];
    
    if (savedData) {
        try {
            savedGrid = JSON.parse(savedData);
            // Si los datos guardados coinciden con las dimensiones actuales, los usamos
            if (savedGrid.length === totalItems) {
                gridData = savedGrid;
                console.log("Datos cargados de localStorage");
            } else {
                console.warn("Las dimensiones de la cuadrícula cambiaron. Se reiniciará la cuadrícula.");
                // Si cambian las dimensiones, no cargamos los viejos datos para evitar errores de índices
            }
        } catch (e) {
            console.error("Error al cargar datos guardados:", e);
        }
    }

    // Si no hay datos guardados o las dimensiones no coinciden, generamos nuevos
    if (gridData.length === 0) {
        for (let i = 0; i < totalItems; i++) {
            gridData.push({
                id: i,
                name: '',
                desc: '',
                capacity: 42,
                color: DEFAULT_COLOR,
                isEmpty: true,
                equipment: []
            });
        }
    }
    

    // Renderizar la cuadrícula visual
    for (let i = 0; i < totalItems; i++) {
        const data = gridData[i];
        const div = document.createElement('div');
        
        div.className = data.isEmpty ? 'grid-item empty' : 'grid-item';
        div.dataset.index = i;
        div.textContent = data.name || 'Vacío';
        
        if (!data.isEmpty) {
            div.style.backgroundColor = data.color;
        }
        
        div.addEventListener('click', () => seleccionarCelda(div, i));
        container.appendChild(div);
    }
}

// ---Funcion de guardado de celdas
function guardarDatos() {
    try {
        localStorage.setItem('dcim_grid_data', JSON.stringify(gridData));
    } catch (e) {
        console.error("Error al guardar en localStorage:", e);
        alert("No se pudo guardar el estado. Quizás el almacenamiento está lleno.");
    }
}

// --- SELECCIÓN DE CÉLULA ---
function seleccionarCelda(element, index) {
    if (selectedElement) selectedElement.classList.remove('selected');
    selectedElement = element;
    selectedIndex = index;
    selectedElement.classList.add('selected');

    const data = gridData[index];
    
    // Rellenar campos principales
    document.getElementById('editName').value = data.name || '';
    document.getElementById('editDesc').value = data.desc || '';
    document.getElementById('editCapacity').value = data.capacity;
    document.getElementById('editColor').value = data.color;

    document.getElementById('emptyState').style.display = 'none';
    document.getElementById('actionForm').style.display = 'block';

    configurarBotones(data.isEmpty);
    
    // Renderizar la cuadrícula vertical de equipos
    renderEquipmentGrid(data);
}

// --- CONFIGURACIÓN DE BOTONES ---
function configurarBotones(isEmpty) {
    const container = document.getElementById('buttonContainer');
    const title = document.getElementById('formTitle');
    container.innerHTML = '';

    if (isEmpty) {
        title.textContent = 'Crear Nueva Celda';
        document.getElementById('editName').focus();
        
        const btnCreate = document.createElement('button');
        btnCreate.className = 'btn btn-primary btn-create';
        btnCreate.textContent = 'Crear Celda';
        btnCreate.onclick = guardarCelda;
        container.appendChild(btnCreate);

        const btnCancel = document.createElement('button');
        btnCancel.className = 'btn btn-cancel';
        btnCancel.textContent = 'Cancelar';
        btnCancel.onclick = cerrarFormulario;
        container.appendChild(btnCancel);
    } else {
        title.textContent = `Editar: ${gridData[selectedIndex].name}`;
        
        const btnSave = document.createElement('button');
        btnSave.className = 'btn btn-primary';
        btnSave.textContent = 'Guardar Cambios';
        btnSave.onclick = guardarCelda;
        container.appendChild(btnSave);

        const btnReset = document.createElement('button');
        btnReset.className = 'btn btn-reset';
        btnReset.textContent = 'Restablecer';
        btnReset.onclick = resetearCelda;
        container.appendChild(btnReset);

        const btnDelete = document.createElement('button');
        btnDelete.className = 'btn btn-delete';
        btnDelete.textContent = 'Borrar Celda';
        btnDelete.onclick = borrarCelda;
        container.appendChild(btnDelete);
    }
}

// --- OPERACIONES DE CÉLULA ---
function guardarCelda() {
    const name = document.getElementById('editName').value.trim();
    if (!name) { alert("El nombre es obligatorio."); return; }

    const desc = document.getElementById('editDesc').value;
    const capacity = parseInt(document.getElementById('editCapacity').value) || 42;
    const color = document.getElementById('editColor').value;

    // Si la capacidad cambia y hay equipos que exceden el nuevo límite, podríamos alertar o recortar.
    // Por simplicidad, solo guardamos.
    
    gridData[selectedIndex] = {
        ...gridData[selectedIndex],
        name: name,
        desc: desc,
        capacity: capacity,
        color: color,
        isEmpty: false
    };

    selectedElement.textContent = name;
    selectedElement.style.backgroundColor = color;
    selectedElement.classList.remove('empty');

    configurarBotones(false);
    renderEquipmentGrid(gridData[selectedIndex]); 
    guardarDatos(); // <--- AÑADIR ESTO para el guardado de los dato en la recarga
    statusMsg("Celda guardada");
}

function resetearCelda() {
    if (!confirm("¿Restablecer esta celda? Se perderán los equipos asociados.")) return;

    const index = selectedIndex;
    const originalName = `${DEFAULT_NAME_PREFIX} ${index + 1}`;
    
    gridData[index] = {
        id: index,
        name: originalName,
        desc: '',
        capacity: 42,
        color: DEFAULT_COLOR,
        isEmpty: false,
        equipment: [] 
    };

    selectedElement.textContent = originalName;
    selectedElement.style.backgroundColor = DEFAULT_COLOR;
    
    document.getElementById('editName').value = originalName;
    document.getElementById('editDesc').value = '';
    document.getElementById('editCapacity').value = 42;
    document.getElementById('editColor').value = DEFAULT_COLOR;
    
    configurarBotones(false);
    renderEquipmentGrid(gridData[selectedIndex]);
    guardarDatos(); // <--- AÑADIR ESTO para el guardado de los dato en la recarga
    statusMsg("Celda restablecida");
}

function borrarCelda() {
    if (!confirm("¿Estás seguro de borrar esta celda? Se eliminarán todos sus equipos.")) return;

    gridData[selectedIndex] = {
        id: selectedIndex,
        name: '', desc: '', capacity: 42, color: DEFAULT_COLOR, isEmpty: true, equipment: []
    };

    selectedElement.textContent = 'Vacío';
    selectedElement.style.backgroundColor = '#e5e7eb';
    selectedElement.classList.add('empty');
    selectedElement.classList.remove('selected');

    cerrarFormulario();
    guardarDatos(); // <--- AÑADIR ESTO para el guardado de los dato en la recarga
    statusMsg("Celda borrada");
}

function cerrarFormulario() {
    if (selectedElement) selectedElement.classList.remove('selected');
    selectedElement = null;
    selectedIndex = -1;
    resetFormUI();
}

// --- RENDERIZADO DE LA CUADRÍCULA VERTICAL (NUEVA LÓGICA) ---
function renderEquipmentGrid(cellData) {
    const gridContainer = document.getElementById('equipmentGrid');
    const section = document.getElementById('equipmentSection');
    const capacityDisplay = document.getElementById('capacityDisplay');
    const btnAdd = document.getElementById('btnAddEquipment');

    if (!gridContainer || !section) return;

    gridContainer.innerHTML = '';
    section.classList.add('visible');
    capacityDisplay.textContent = cellData.capacity;

    // Si la celda está vacía, no mostrar nada ni botón
    if (cellData.isEmpty) {
        btnAdd.style.display = 'none';
        const msg = document.createElement('div');
        msg.className = 'eq-slot';
        msg.style.justifyContent = 'center';
        msg.style.height = '40px';
        msg.style.color = '#999';
        msg.style.fontStyle = 'italic';
        msg.textContent = 'Crea la celda primero';
        gridContainer.appendChild(msg);
        return;
    }

    btnAdd.style.display = 'block';

    // Calcular la posición de los equipos
    // Ordenamos equipos por su posición actual (topU) para dibujar de abajo a arriba
    // Nota: El CSS tiene flex-direction: column-reverse, así que el primer elemento del DOM estará abajo (U1)
    // Pero lógicamente, U1 es el índice 0.
    
    // Vamos a crear un array de "slots" vacíos
    let slots = new Array(cellData.capacity).fill(null);
    
    // Colocar equipos en el array de slots
    // Asumimos que los equipos ya tienen 'topU' calculado. Si no, intentamos calcularlo.
    // Para simplificar, si no hay 'topU', lo calculamos basado en el orden actual (primero disponible).
    
    // Primero, calculamos posiciones si faltan (algoritmo de empaquetado simple: primer hueco libre)
    let tempSlots = new Array(cellData.capacity).fill(false);
    
    // Ordenamos equipos actuales para procesarlos de abajo a arriba (U1 a Un)
    const sortedEquipments = [...cellData.equipment].sort((a, b) => a.topU - b.topU);

    sortedEquipments.forEach(eq => {
        if (eq.topU === undefined) {
            // Calcular posición si no existe (primero disponible)
            let pos = 0;
            while (pos < cellData.capacity) {
                if (!tempSlots[pos]) {
                    // Verificar si cabe
                    let fits = true;
                    for (let k = 0; k < eq.height; k++) {
                        if (pos + k >= cellData.capacity || tempSlots[pos + k]) {
                            fits = false;
                            break;
                        }
                    }
                    if (fits) {
                        eq.topU = pos;
                        for (let k = 0; k < eq.height; k++) tempSlots[pos + k] = true;
                        break;
                    }
                }
                pos++;
            }
        } else {
            // Marcar slots como ocupados
            for (let k = 0; k < eq.height; k++) {
                if (eq.topU + k < cellData.capacity) {
                    tempSlots[eq.topU + k] = true;
                }
            }
        }
    });

    // Generar el DOM
    // Como usamos column-reverse, el índice 0 (U1) debe ser el ÚLTIMO hijo en el DOM para estar abajo visualmente?
    // No, flex-direction: column-reverse invierte el orden visual.
    // Si queremos que U1 (índice 0) esté abajo, y el índice 1 esté arriba, 
    // y usamos column-reverse, entonces el DOM debe tener: [U1, U2, U3...] -> Visualmente: U3 (arriba) ... U1 (abajo).
    // Espera, column-reverse: el primer elemento del DOM va al final del contenedor (abajo).
    // Entonces, si el DOM es [Slot0, Slot1, Slot2], visualmente:
    // Slot2 (arriba)
    // Slot1
    // Slot0 (abajo)
    // Esto es confuso. Mejor usamos column normal y calculamos el orden inverso al pintar.
    // O usamos column-reverse y pintamos de N a 0.
    
    // Vamos a usar column-reverse (como está en CSS) y pintar desde capacity-1 hasta 0.
    // Así el índice 0 (U1) será el último elemento añadido, y aparecerá abajo.
    // El índice Capacity-1 será el primero añadido, aparecerá arriba.

    for (let i = cellData.capacity - 1; i >= 0; i--) {
        const slot = document.createElement('div');
        slot.className = 'eq-slot';
        slot.dataset.u = i;
        
        // Buscar si hay un equipo ocupando este slot
        // Un equipo ocupa i si eq.topU <= i < eq.topU + eq.height
        let occupiedBy = null;
        for (let eq of cellData.equipment) {
            if (i >= eq.topU && i < eq.topU + eq.height) {
                occupiedBy = eq;
                break;
            }
        }

        if (occupiedBy) {
            slot.classList.add('occupied');
            // Color: si el equipo tiene color propio (podríamos añadirlo al objeto eq), usarlo, sino el de la celda
            // Por ahora usamos el color de la celda para todo, o un color genérico
            slot.style.backgroundColor = cellData.color; 
            
            // Solo mostrar nombre en la parte superior del equipo (el primer U que ocupa)
            if (i === occupiedBy.topU) {
                const nameSpan = document.createElement('span');
                nameSpan.className = 'eq-slot-name';
                nameSpan.textContent = occupiedBy.name;
                slot.appendChild(nameSpan);
                
                const numSpan = document.createElement('span');
                numSpan.className = 'eq-slot-number';
                numSpan.textContent = `${occupiedBy.topU + 1}U`; // U1 es índice 0
                slot.appendChild(numSpan);
            } else {
                // Solo mostrar número en las partes inferiores
                const numSpan = document.createElement('span');
                numSpan.className = 'eq-slot-number';
                numSpan.textContent = i + 1;
                slot.appendChild(numSpan);
            }
            
            // Evento click en el equipo
            slot.onclick = () => abrirModalEquipo(cellData.equipment.indexOf(occupiedBy));
        } else {
            // Slot vacío
            slot.innerHTML = `<span class="eq-slot-number">${i + 1}</span>`;
            slot.onclick = () => {
                // Intentar añadir equipo en la primera posición libre a partir de i
                // Buscamos el primer hueco libre desde abajo (o desde i?)
                // Para simplificar: si haces click en un hueco vacío, intentamos poner el equipo ahí si cabe.
                abrirModalEquipo(null, i);
            };
        }
        
        gridContainer.appendChild(slot);
    }
}

// --- GESTIÓN DE EQUIPOS ---
function abrirModalEquipo(index, suggestedTopU = null) {
    const modal = document.getElementById('equipmentModal');
    const title = document.getElementById('eqModalTitle');
    const btnContainer = document.getElementById('modalActions');
    
    if (!btnContainer) {
        console.error("Error: No se encontró el contenedor de acciones del modal.");
        return;
    }
    btnContainer.innerHTML = ''; 

    modal.style.display = 'flex';

    // --- Inyección dinámica del campo de Altura si no existe ---
    let heightField = document.getElementById('eqHeight');
    if (!heightField) {
        heightField = document.createElement('div');
        heightField.className = 'form-group';
        heightField.innerHTML = `
            <label>Altura (U): 
                <input type="number" id="eqHeight" value="1" min="1" max="42">
            </label>
        `;
        const compField = document.getElementById('eqComp').parentElement;
        compField.parentNode.insertBefore(heightField, compField.nextSibling);
    }

    // Referencia segura a la celda actual
    const cellData = gridData[selectedIndex];
    
    if (!cellData) {
        alert("Error: No hay una celda seleccionada.");
        closeEquipmentModal();
        return;
    }

    if (index === null) {
        // --- MODO CREAR ---
        title.textContent = "Crear Equipo";
        document.getElementById('eqName').value = '';
        document.getElementById('eqDesc').value = '';
        document.getElementById('eqFunc').value = '';
        document.getElementById('eqComp').value = '';
        document.getElementById('eqHeight').value = 1;

        const btnSave = document.createElement('button');
        btnSave.className = 'btn btn-primary';
        btnSave.textContent = 'Guardar Equipo';
        
        const btnCancelar = document.createElement('button');
        btnCancelar.className = 'btn btn-cancel';
        btnCancelar.textContent = 'Cancelar';
        btnCancelar.onclick = closeEquipmentModal;

        btnContainer.appendChild(btnCancelar);
        btnContainer.appendChild(btnSave);

        btnSave.onclick = () => {
            const name = document.getElementById('eqName').value.trim();
            const height = parseInt(document.getElementById('eqHeight').value) || 1;
            
            if (!name) { alert("Nombre requerido"); return; }

            let topU = suggestedTopU !== null ? suggestedTopU : 0;

            // Buscar el primer hueco libre válido
            if (suggestedTopU === null) {
                let found = false;
                for (let i = 0; i <= cellData.capacity - height; i++) {
                    let fits = true;
                    for (let k = 0; k < height; k++) {
                        if (cellData.equipment.some(eq => 
                            i + k >= eq.topU && i + k < eq.topU + eq.height
                        )) {
                            fits = false;
                            break;
                        }
                    }
                    if (fits) {
                        topU = i;
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    alert("No hay espacio suficiente para un equipo de " + height + "U.");
                    return;
                }
            } else {
                // Validar posición sugerida
                let fits = true;
                for (let k = 0; k < height; k++) {
                    if (topU + k >= cellData.capacity) {
                        fits = false;
                        break;
                    }
                    if (cellData.equipment.some(eq => 
                        topU + k >= eq.topU && topU + k < eq.topU + eq.height
                    )) {
                        fits = false;
                        break;
                    }
                }
                if (!fits) {
                    alert("El espacio sugerido está ocupado. Buscando siguiente hueco libre...");
                    let found = false;
                    for (let i = topU; i <= cellData.capacity - height; i++) {
                        let fitsLocal = true;
                        for (let k = 0; k < height; k++) {
                            if (i + k >= cellData.capacity) {
                                fitsLocal = false;
                                break;
                            }
                            if (cellData.equipment.some(eq => 
                                i + k >= eq.topU && i + k < eq.topU + eq.height
                            )) {
                                fitsLocal = false;
                                break;
                            }
                        }
                        if (fitsLocal) {
                            topU = i;
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        alert("No hay espacio disponible para un equipo de " + height + "U.");
                        return;
                    }
                }
            }

            cellData.equipment.push({
                name: name,
                desc: document.getElementById('eqDesc').value,
                func: document.getElementById('eqFunc').value,
                comp: document.getElementById('eqComp').value,
                height: height,
                topU: topU
            });
            guardarDatos(); // <--- AÑADIR ESTO para el guardado de los dato en la recarga
            modal.style.display = 'none';
            renderEquipmentGrid(cellData);
            statusMsg("Equipo añadido");
        };
    } else {
        // --- MODO EDITAR ---
        const equipment = cellData.equipment[index];
        if (!equipment) {
            alert("Error: Equipo no encontrado.");
            closeEquipmentModal();
            return;
        }

        title.textContent = `Editar Equipo: ${equipment.name}`;
        document.getElementById('eqName').value = equipment.name;
        document.getElementById('eqDesc').value = equipment.desc || '';
        document.getElementById('eqFunc').value = equipment.func || '';
        document.getElementById('eqComp').value = equipment.comp || '';
        document.getElementById('eqHeight').value = equipment.height || 1;

        const btnSave = document.createElement('button');
        btnSave.className = 'btn btn-primary';
        btnSave.textContent = 'Guardar Cambios';

        const btnDelete = document.createElement('button');
        btnDelete.className = 'btn btn-delete';
        btnDelete.textContent = 'Borrar';
        btnDelete.onclick = () => {
            if (confirm(`¿Estás seguro de borrar "${equipment.name}"?`)) {
                cellData.equipment.splice(index, 1);
                guardarDatos(); // <--- AÑADIR ESTO para el guardado de los dato en la recarga
                modal.style.display = 'none';
                renderEquipmentGrid(cellData);
                statusMsg("Equipo eliminado");
            }
        };

        const btnCancelar = document.createElement('button');
        btnCancelar.className = 'btn btn-cancel';
        btnCancelar.textContent = 'Cancelar';
        btnCancelar.onclick = closeEquipmentModal;

        btnContainer.appendChild(btnCancelar);
        btnContainer.appendChild(btnDelete);
        btnContainer.appendChild(btnSave);

        btnSave.onclick = () => {
            const name = document.getElementById('eqName').value.trim();
            const height = parseInt(document.getElementById('eqHeight').value) || 1;
            
            if (!name) { alert("Nombre requerido"); return; }

            let currentTopU = equipment.topU;
            let testTopU = currentTopU;
            let fits = true;

            // 1. Intentar mantener posición actual si es posible
            for (let k = 0; k < height; k++) {
                if (testTopU + k >= cellData.capacity) {
                    fits = false;
                    break;
                }
                // Chequear colisión ignorando al propio equipo (index)
                if (cellData.equipment.some((eq, idx) => 
                    idx !== index && (testTopU + k >= eq.topU && testTopU + k < eq.topU + eq.height)
                )) {
                    fits = false;
                    break;
                }
            }

            // 2. Si no cabe, buscar nuevo hueco libre
            if (!fits) {
                let found = false;
                for (let i = 0; i <= cellData.capacity - height; i++) {
                    let fitsLocal = true;
                    for (let k = 0; k < height; k++) {
                        if (i + k >= cellData.capacity) {
                            fitsLocal = false;
                            break;
                        }
                        if (cellData.equipment.some((eq, idx) => 
                            idx !== index && (i + k >= eq.topU && i + k < eq.topU + eq.height)
                        )) {
                            fitsLocal = false;
                            break;
                        }
                    }
                    if (fitsLocal) {
                        testTopU = i;
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    alert("No hay espacio disponible con la nueva altura de " + height + "U.");
                    return;
                }
            }

            // Actualizar el equipo en el array
            cellData.equipment[index] = {
                ...equipment,
                name: name,
                desc: document.getElementById('eqDesc').value,
                func: document.getElementById('eqFunc').value,
                comp: document.getElementById('eqComp').value,
                height: height,
                topU: testTopU
            };
            guardarDatos(); // <--- AÑADIR ESTO para el guardado de los dato en la recarga
            modal.style.display = 'none';
            renderEquipmentGrid(cellData);
            statusMsg("Equipo actualizado");
        };
    }
}

function closeEquipmentModal() {
    document.getElementById('equipmentModal').style.display = 'none';
    if (selectedIndex !== -1) {
        renderEquipmentGrid(gridData[selectedIndex]);
    }
}

// --- UTILIDADES ---
function resetFormUI() {
    document.getElementById('editName').value = '';
    document.getElementById('editDesc').value = '';
    document.getElementById('editCapacity').value = 42;
    document.getElementById('editColor').value = DEFAULT_COLOR;
    document.getElementById('buttonContainer').innerHTML = '';
    
    const eqList = document.getElementById('equipmentGrid');
    const eqSection = document.getElementById('equipmentSection');
    
    if (eqList) eqList.innerHTML = '';
    if (eqSection) eqSection.classList.remove('visible');
    
    // Limpiar campo de altura si se inyectó
    const heightField = document.getElementById('eqHeight');
    if (heightField) {
        const parent = heightField.parentElement;
        if (parent) parent.remove();
    }
}

function statusMsg(msg) {
    const header = document.querySelector('header .controls');
    if (!header) return;
    
    // Eliminar mensajes anteriores
    const existing = header.querySelector('.status-msg');
    if (existing) existing.remove();
    
    const status = document.createElement('span');
    status.className = 'status-msg';
    status.textContent = `✓ ${msg}`;
    status.style.marginLeft = '10px';
    status.style.color = '#10b981';
    status.style.fontWeight = 'bold';
    status.style.fontSize = '0.9rem';
    header.appendChild(status);
    
    setTimeout(() => {
        if (status.parentNode) status.remove();
    }, 2500);
}

// Inicializar al cargar
window.addEventListener('DOMContentLoaded', generarCuadricula);