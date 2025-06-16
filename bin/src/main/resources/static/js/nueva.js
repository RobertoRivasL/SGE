
console.log('nueva.js cargado correctamente');

// Variables globales
let productosAgregados = [];
const IVA_RATE = 0.19;
let modalBuscarProducto;

document.addEventListener('DOMContentLoaded', function() {
    console.log('Document ready ejecutado');
    inicializarComponentes();
    configurarEventListeners();
});

function inicializarComponentes() {
    inicializarSelect2();
    configurarFechaActual();
    modalBuscarProducto = new bootstrap.Modal(document.getElementById('modalBuscarProducto'));
}

function inicializarSelect2() {
    $('.select2').select2({
        theme: 'bootstrap-5',
        language: 'es'
    });

    $('#selectorProducto').select2({
        theme: 'bootstrap-5',
        language: 'es',
        placeholder: 'Seleccione un producto',
        allowClear: true
    });
}

function configurarFechaActual() {
    const inputFecha = document.querySelector('input[type="datetime-local"]');
    if (inputFecha && !inputFecha.value) {
        const ahora = new Date();
        ahora.setMinutes(ahora.getMinutes() - ahora.getTimezoneOffset());
        inputFecha.value = ahora.toISOString().slice(0, 16);
    }
}

function configurarEventListeners() {
    document.getElementById('formVenta').addEventListener('submit', validarFormulario);
    document.getElementById('selectorProducto').addEventListener('change', manejarSeleccionProducto);
    document.getElementById('btnAgregarProducto').addEventListener('click', agregarProductoSeleccionado);
    document.getElementById('descuentoAdicional').addEventListener('input', calcularTotales);
    document.getElementById('pagoRecibido').addEventListener('input', calcularVuelto);
    document.getElementById('busquedaProducto').addEventListener('input', debounce(buscarProductos, 300));
}

// Funciones de navegación y validación
function siguientePaso(paso) {
    if (!validarPaso(paso - 1)) return;
    actualizarIndicadoresPaso(paso);
    mostrarPanelActual(paso);
    if (paso === 3) generarResumenConfirmacion();
}

function anteriorPaso(paso) {
    actualizarIndicadoresPaso(paso);
    mostrarPanelActual(paso);
}

function validarPaso(paso) {
    switch(paso) {
        case 1: return validarPaso1();
        case 2: return validarPaso2();
        default: return true;
    }
}

function validarPaso1() {
    const clienteId = document.querySelector('[name="clienteId"]').value;
    const fecha = document.querySelector('[name="fecha"]').value;
    if (!clienteId) {
        mostrarError('Debe seleccionar un cliente');
        return false;
    }
    if (!fecha) {
        mostrarError('Debe seleccionar una fecha válida');
        return false;
    }
    return true;
}

function validarPaso2() {
    if (productosAgregados.length === 0) {
        mostrarError('Debe agregar al menos un producto');
        return false;
    }
    return true;
}

function actualizarIndicadoresPaso(pasoActual) {
    document.querySelectorAll('.step').forEach((step, index) => {
        if (index + 1 < pasoActual) {
            step.classList.add('completed');
            step.classList.remove('active');
        } else if (index + 1 === pasoActual) {
            step.classList.add('active');
            step.classList.remove('completed');
        } else {
            step.classList.remove('active', 'completed');
        }
    });
}

function mostrarPanelActual(paso) {
    document.querySelectorAll('[id^="panel-step"]').forEach(panel => {
        panel.style.display = 'none';
    });
    document.getElementById(`panel-step${paso}`).style.display = 'block';
}

// Funciones de manejo de productos
function manejarSeleccionProducto() {
    const selector = document.getElementById('selectorProducto');
    const cantidadInput = document.getElementById('cantidadProducto');
    if (selector.value) {
        cantidadInput.disabled = false;
        cantidadInput.max = selector.options[selector.selectedIndex].dataset.stock;
    } else {
        cantidadInput.disabled = true;
        cantidadInput.value = '';
    }
}

function agregarProductoSeleccionado() {
    const selector = document.getElementById('selectorProducto');
    const cantidad = document.getElementById('cantidadProducto');

    if (!selector.value) {
        mostrarError('Seleccione un producto');
        return;
    }

    const option = selector.options[selector.selectedIndex];
    const {id, nombre, precio, stock} = option.dataset;
    const cantidadSeleccionada = parseInt(cantidad.value);

    if (cantidadSeleccionada > parseInt(stock)) {
        mostrarError('La cantidad seleccionada supera el stock disponible');
        return;
    }

    agregarProducto({
        id: parseInt(id),
        nombre,
        precio: parseFloat(precio),
        stock: parseInt(stock),
        cantidad: cantidadSeleccionada,
        descuento: 0
    });

    actualizarInterfaz();
    limpiarSeleccionProducto();
}

function agregarProducto(producto) {
    const productoExistente = productosAgregados.find(p => p.id === producto.id);
    if (productoExistente) {
        productoExistente.cantidad += producto.cantidad;
    } else {
        productosAgregados.push(producto);
    }
}

function actualizarInterfaz() {
    actualizarListaProductos();
    calcularTotales();
    actualizarEstadisticas();
}

function actualizarListaProductos() {
    const lista = document.getElementById('listaProductos');
    const mensaje = document.getElementById('mensajeVacio');

    if (productosAgregados.length === 0) {
        mensaje.style.display = 'block';
        document.getElementById('btnSiguienteProductos').disabled = true;
        lista.innerHTML = '';
        return;
    }

    mensaje.style.display = 'none';
    document.getElementById('btnSiguienteProductos').disabled = false;

    lista.innerHTML = productosAgregados.map((producto, index) => `
        <div class="producto-item" data-index="${index}">
            <div class="row align-items-center">
                <div class="col-md-4">
                    <strong>${producto.nombre}</strong>
                    <div class="small text-muted">Stock: ${producto.stock}</div>
                </div>
                <div class="col-md-2">
                    <input type="number" class="form-control form-control-sm"
                           value="${producto.cantidad}" min="1" max="${producto.stock}"
                           onchange="actualizarCantidad(${index}, this.value)">
                </div>
                <div class="col-md-2">
                    <div class="small">Precio Unit.</div>
                    <strong>$${producto.precio.toFixed(2)}</strong>
                </div>
                <div class="col-md-2">
                    <div class="small">Descuento</div>
                    <input type="number" class="form-control form-control-sm"
                           value="${producto.descuento}" min="0" max="100" step="0.1"
                           onchange="actualizarDescuento(${index}, this.value)">
                </div>
                <div class="col-md-1">
                    <div class="small">Subtotal</div>
                    <strong class="text-success">$${calcularSubtotalProducto(producto).toFixed(2)}</strong>
                </div>
                <div class="col-md-1">
                    <button type="button" class="btn btn-outline-danger btn-sm"
                            onclick="eliminarProducto(${index})">
                        <i class="fas fa-trash"></i>
                    </button>
                </div>
            </div>
        </div>
    `).join('');

    actualizarCamposOcultos();
}

function calcularSubtotalProducto(producto) {
    return (producto.precio * producto.cantidad) * (1 - producto.descuento/100);
}

function actualizarCantidad(index, nuevaCantidad) {
    const producto = productosAgregados[index];
    const cantidad = parseInt(nuevaCantidad);

    if (cantidad > producto.stock) {
        mostrarError(`Stock insuficiente. Máximo: ${producto.stock}`);
        document.querySelector(`[data-index="${index}"] input[type="number"]`).value = producto.cantidad;
        return;
    }

    producto.cantidad = cantidad;
    actualizarInterfaz();
}

function actualizarDescuento(index, nuevoDescuento) {
    productosAgregados[index].descuento = parseFloat(nuevoDescuento) || 0;
    actualizarInterfaz();
}

function eliminarProducto(index) {
    if (confirm('¿Está seguro de eliminar este producto?')) {
        productosAgregados.splice(index, 1);
        actualizarInterfaz();
    }
}

// Funciones de cálculo y formato
function calcularTotales() {
    const subtotal = productosAgregados.reduce((total, producto) => total + calcularSubtotalProducto(producto), 0);
    const descuentoAdicional = parseFloat(document.getElementById('descuentoAdicional').value) || 0;
    const descuento = subtotal * (descuentoAdicional / 100);
    const iva = (subtotal - descuento) * IVA_RATE;
    const total = subtotal - descuento + iva;

    document.getElementById('subtotalDisplay').textContent = formatearMoneda(subtotal);
    document.getElementById('descuentosDisplay').textContent = formatearMoneda(descuento);
    document.getElementById('ivaDisplay').textContent = formatearMoneda(iva);
    document.getElementById('totalDisplay').textContent = formatearMoneda(total);
}

function formatearMoneda(valor) {
    return new Intl.NumberFormat('es-CL', { style: 'currency', currency: 'CLP' }).format(valor);
}

function actualizarEstadisticas() {
    document.getElementById('cantidadArticulos').textContent = productosAgregados.reduce((total, producto) => total + producto.cantidad, 0);
    document.getElementById('productosUnicos').textContent = productosAgregados.length;
}

function calcularVuelto() {
    const total = parseFloat(document.getElementById('totalDisplay').textContent.replace(/[^\d.-]/g, ''));
    const pago = parseFloat(document.getElementById('pagoRecibido').value) || 0;
    const vuelto = Math.max(0, pago - total);
    document.getElementById('vueltoDisplay').textContent = formatearMoneda(vuelto);
}

function actualizarCamposOcultos() {
    const container = document.getElementById('camposOcultos');
    container.innerHTML = productosAgregados.map((producto, index) => `
        <input type="hidden" name="detalles[${index}].productoId" value="${producto.id}">
        <input type="hidden" name="detalles[${index}].cantidad" value="${producto.cantidad}">
        <input type="hidden" name="detalles[${index}].precioUnitario" value="${producto.precio}">
        <input type="hidden" name="detalles[${index}].descuento" value="${producto.descuento}">
    `).join('');
}

// Funciones de búsqueda y selección de productos
function buscarProducto() {
    modalBuscarProducto.show();
    document.getElementById('resultadosBusqueda').innerHTML = '';
    document.getElementById('busquedaProducto').focus();
}

function buscarProductos() {
    const termino = document.getElementById('busquedaProducto').value.trim();
    if (termino.length < 3) {
        document.getElementById('resultadosBusqueda').innerHTML = '<p>Ingrese al menos 3 caracteres para buscar.</p>';
        return;
    }

    fetch(`/api/productos/buscar?termino=${encodeURIComponent(termino)}`)
        .then(response => response.json())
        .then(productos => {
            mostrarResultadosBusqueda(productos);
        })
        .catch(error => {
            console.error('Error:', error);
            document.getElementById('resultadosBusqueda').innerHTML = '<p>Error al buscar productos. Intente nuevamente.</p>';
        });
}

function mostrarResultadosBusqueda(productos) {
    const contenedor = document.getElementById('resultadosBusqueda');
    if (productos.length === 0) {
        contenedor.innerHTML = '<p>No se encontraron productos.</p>';
        return;
    }

    const listaHTML = productos.map(producto => `
        <div class="list-group-item list-group-item-action">
            <div class="d-flex w-100 justify-content-between">
                <h5 class="mb-1">${producto.nombre}</h5>
                <small>Stock: ${producto.stock}</small>
            </div>
            <p class="mb-1">Precio: ${formatearMoneda(producto.precio)}</p>
            <button class="btn btn-sm btn-primary" onclick="seleccionarProducto(${JSON.stringify(producto).replace(/"/g, '&quot;')})">
                Seleccionar
            </button>
        </div>
    `).join('');

    contenedor.innerHTML = listaHTML;
}

function seleccionarProducto(producto) {
    agregarProductoAVenta(producto);
    modalBuscarProducto.hide();
}

function agregarProductoAVenta(producto) {
    agregarProducto({
        id: producto.id,
        nombre: producto.nombre,
        precio: producto.precio,
        stock: producto.stock,
        cantidad: 1,
        descuento: 0
    });

    actualizarInterfaz();
}

// Funciones de utilidad
function limpiarVenta() {
    if (confirm('¿Está seguro de limpiar toda la venta?')) {
        productosAgregados = [];
        actualizarInterfaz();
        anteriorPaso(1);
    }
}

function guardarBorrador() {
    // Implementar lógica para guardar borrador
    alert('Funcionalidad de borrador no implementada aún');
}

function limpiarSeleccionProducto() {
    document.getElementById('selectorProducto').value = '';
    document.getElementById('cantidadProducto').value = '';
    $('#selectorProducto').trigger('change'); // Para actualizar Select2
}

function validarFormulario(e) {
    if (productosAgregados.length === 0) {
        e.preventDefault();
        mostrarError('Debe agregar al menos un producto');
        return;
    }

    const clienteId = document.querySelector('[name="clienteId"]').value;
    if (!clienteId) {
        e.preventDefault();
        mostrarError('Debe seleccionar un cliente');
        return;
    }

    actualizarCamposOcultos();
}

function mostrarError(mensaje) {
    // Implementar una función para mostrar errores de manera más amigable
    // Por ejemplo, usando un modal de Bootstrap o una librería de notificaciones
    alert(mensaje);
}

function generarResumenConfirmacion() {
    const cliente = document.querySelector('[name="clienteId"] option:checked').textContent;
    const vendedor = document.querySelector('[name="vendedorId"] option:checked').textContent;
    const metodoPago = document.querySelector('[name="metodoPago"] option:checked').textContent;

    document.getElementById('resumenConfirmacion').innerHTML = `
        <div class="row">
            <div class="col-md-6">
                <h6>Información General</h6>
                <p><strong>Cliente:</strong> ${cliente}</p>
                <p><strong>Vendedor:</strong> ${vendedor}</p>
                <p><strong>Método de Pago:</strong> ${metodoPago}</p>
            </div>
            <div class="col-md-6">
                <h6>Totales</h6>
                <p><strong>Subtotal:</strong> ${document.getElementById('subtotalDisplay').textContent}</p>
                <p><strong>Descuentos:</strong> ${document.getElementById('descuentosDisplay').textContent}</p>
                <p><strong>IVA:</strong> ${document.getElementById('ivaDisplay').textContent}</p>
                <p><strong>Total:</strong> <span class="h5 text-success">${document.getElementById('totalDisplay').textContent}</span></p>
            </div>
        </div>
        <h6 class="mt-3">Productos (${productosAgregados.length})</h6>
        <div class="table-responsive">
            <table class="table table-sm">
                <thead>
                    <tr>
                        <th>Producto</th>
                        <th>Cantidad</th>
                        <th>Precio</th>
                        <th>Descuento</th>
                        <th>Subtotal</th>
                    </tr>
                </thead>
                <tbody>
                    ${productosAgregados.map(p => `
                        <tr>
                            <td>${p.nombre}</td>
                            <td>${p.cantidad}</td>
                            <td>${formatearMoneda(p.precio)}</td>
                            <td>${p.descuento}%</td>
                            <td>${formatearMoneda(calcularSubtotalProducto(p))}</td>
                        </tr>
                    `).join('')}
                </tbody>
            </table>
        </div>
    `;
}

// Función de debounce para evitar múltiples llamadas durante la escritura
function debounce(func, wait) {
    let timeout;
    return function executedFunction(...args) {
        const later = () => {
            clearTimeout(timeout);
            func(...args);
        };
        clearTimeout(timeout);
        timeout = setTimeout(later, wait);
    };
}

// Exportar funciones que necesitan ser accesibles globalmente
window.siguientePaso = siguientePaso;
window.anteriorPaso = anteriorPaso;
window.actualizarCantidad = actualizarCantidad;
window.actualizarDescuento = actualizarDescuento;
window.eliminarProducto = eliminarProducto;
window.calcularVuelto = calcularVuelto;
window.buscarProducto = buscarProducto;
window.limpiarVenta = limpiarVenta;
window.guardarBorrador = guardarBorrador;
window.seleccionarProducto = seleccionarProducto;