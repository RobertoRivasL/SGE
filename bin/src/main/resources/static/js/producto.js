// src/main/resources/static/js/productos.js

// --- DECLARACIÓN DE CONSTANTES Y REFERENCIAS DOM ---
const API_URL_PRODUCTOS = '/api/productos';

const formularioProducto = document.getElementById('formularioProducto');
const inputCodigo = document.getElementById('codigo');
const inputNombre = document.getElementById('nombre');
const inputDescripcion = document.getElementById('descripcion');
const inputPrecio = document.getElementById('precio');
const inputStock = document.getElementById('stock');
const inputMarca = document.getElementById('marca');
const inputModelo = document.getElementById('modelo');

const mensajeFormulario = document.getElementById('mensajeFormulario');
const cuerpoTablaProductos = document.querySelector('#tablaProductos tbody');
const mensajeLista = document.getElementById('mensajeLista');

// --- FUNCIONES FETCH CON COOKIES DE SESIÓN ---
async function obtenerProductos() {
    try {
        const respuesta = await fetch(API_URL_PRODUCTOS, {credentials: 'same-origin'});

        if (!respuesta.ok) {
            if (respuesta.status === 401 || respuesta.status === 403) {
                window.location.href = '/login';
            }
            throw new Error(`Error HTTP al obtener productos: ${respuesta.status}`);
        }

        return await respuesta.json();
    } catch (error) {
        console.error("Error al obtener productos:", error);
        mensajeLista.textContent = 'Error al cargar productos.';
        return [];
    }
}

async function guardarProducto(producto) {
    try {
        mensajeFormulario.textContent = 'Guardando...';
        mensajeFormulario.className = 'mensaje';

        const respuesta = await fetch(API_URL_PRODUCTOS, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            credentials: 'same-origin',
            body: JSON.stringify(producto)
        });

        if (!respuesta.ok) {
            const errorTexto = await respuesta.text();
            mensajeFormulario.textContent = `Error: ${respuesta.status} ${errorTexto.substring(0, 100)}...`;
            mensajeFormulario.className = 'mensaje error';
            throw new Error(errorTexto);
        }

        const productoGuardado = await respuesta.json();
        mensajeFormulario.textContent = 'Producto guardado correctamente.';
        mensajeFormulario.className = 'mensaje exito';
        cargarProductos();
        return productoGuardado;

    } catch (error) {
        console.error("Error al guardar producto:", error);
        mensajeFormulario.textContent = 'Error al guardar producto.';
        mensajeFormulario.className = 'mensaje error';
    }
}

// --- MOSTRAR LISTA DE PRODUCTOS ---
function mostrarProductos(lista) {
    cuerpoTablaProductos.innerHTML = '';

    if (!Array.isArray(lista) || lista.length === 0) {
        mensajeLista.textContent = 'No hay productos registrados.';
        return;
    }

    mensajeLista.textContent = '';

    lista.forEach(producto => {
        const fila = cuerpoTablaProductos.insertRow();
        fila.insertCell(0).textContent = producto.codigo;
        fila.insertCell(1).textContent = producto.nombre;
        fila.insertCell(2).textContent = producto.descripcion || '-';
        fila.insertCell(3).textContent = `$${producto.precio?.toFixed(2) || '0.00'}`;
        fila.insertCell(4).textContent = producto.stock ?? 0;
        fila.insertCell(5).textContent = producto.marca || '-';
        fila.insertCell(6).textContent = producto.modelo || '-';
        fila.insertCell(7).textContent = producto.activo ? 'Sí' : 'No';

        // Celda de acciones
        const acciones = fila.insertCell(8);
        acciones.innerHTML = `
            <button class="btn btn-sm btn-primary me-1" onclick="editarProducto(${producto.id})">Editar</button>
            <button class="btn btn-sm btn-danger" onclick="eliminarProducto(${producto.id})">Eliminar</button>
        `;
    });
}

async function eliminarProducto(id) {
    if (!confirm('¿Estás seguro que deseas eliminar este producto?')) return;

    try {
        const respuesta = await fetch(`${API_URL_PRODUCTOS}/${id}`, {
            method: 'DELETE',
            credentials: 'same-origin'
        });

        if (!respuesta.ok) {
            throw new Error(`Error al eliminar. Estado: ${respuesta.status}`);
        }

        cargarProductos();
    } catch (error) {
        console.error("Error al eliminar producto:", error);
        alert("No se pudo eliminar el producto.");
    }
}

async function editarProducto(id) {
    try {
        const respuesta = await fetch(`${API_URL_PRODUCTOS}/${id}`, {
            method: 'GET',
            credentials: 'same-origin'
        });

        if (!respuesta.ok) throw new Error('No se pudo cargar el producto.');

        const producto = await respuesta.json();

        // Llenar el formulario con los datos existentes
        inputCodigo.value = producto.codigo;
        inputNombre.value = producto.nombre;
        inputDescripcion.value = producto.descripcion;
        inputPrecio.value = producto.precio;
        inputStock.value = producto.stock;
        inputMarca.value = producto.marca;
        inputModelo.value = producto.modelo;

        // Guardar el ID en un atributo oculto del formulario
        formularioProducto.setAttribute('data-edit-id', producto.id);

        mensajeFormulario.textContent = 'Editando producto...';
        document.getElementById('btnCancelarEdicion').classList.remove('d-none');
    } catch (error) {
        console.error("Error al cargar para editar:", error);
        alert("No se pudo cargar el producto.");
    }
}


// --- EVENTOS ---
formularioProducto.addEventListener('submit', async (e) => {
    e.preventDefault();

    const nuevoProducto = {
        codigo: inputCodigo.value.trim(),
        nombre: inputNombre.value.trim(),
        descripcion: inputDescripcion.value.trim(),
        precio: parseFloat(inputPrecio.value),
        stock: parseInt(inputStock.value),
        marca: inputMarca.value.trim(),
        modelo: inputModelo.value.trim()
    };

    if (!nuevoProducto.codigo || !nuevoProducto.nombre || isNaN(nuevoProducto.precio)) {
        mensajeFormulario.textContent = 'Completa al menos código, nombre y precio válidos.';
        mensajeFormulario.className = 'mensaje error';
        return;
    }

    const idEdicion = formularioProducto.getAttribute('data-edit-id');
    if (idEdicion) {
        await actualizarProducto(idEdicion, nuevoProducto);
        formularioProducto.removeAttribute('data-edit-id');
    } else {
        await guardarProducto(nuevoProducto);
    }

    // Ocultar el botón de cancelar después de guardar o actualizar
    document.getElementById('btnCancelarEdicion').classList.add('d-none'); // Aquí es donde lo agregas
});


async function cargarProductos() {
    const productos = await obtenerProductos();
    mostrarProductos(productos);
}

// --- INICIALIZACIÓN ---
window.addEventListener('DOMContentLoaded', cargarProductos);

const idEdicion = formularioProducto.getAttribute('data-edit-id');
if (idEdicion) {
    await actualizarProducto(idEdicion, nuevoProducto);
    formularioProducto.removeAttribute('data-edit-id');
} else {
    await guardarProducto(nuevoProducto);
}

document.getElementById('btnCancelarEdicion').addEventListener('click', () => {
    formularioProducto.reset();
    formularioProducto.removeAttribute('data-edit-id');
    mensajeFormulario.textContent = 'Edición cancelada.';
    document.getElementById('btnCancelarEdicion').classList.add('d-none');
    setTimeout(() => mensajeFormulario.textContent = '', 3000);
});
