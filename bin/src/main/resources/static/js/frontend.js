/**
 * Utilidades para validaciones en el frontend
 * Integra con el ValidacionControlador del backend
 *
 * @author Roberto Rivas
 * @version 2.0
 */

class ValidacionManager {
    constructor() {
        this.baseUrl = '/api/validaciones';
        this.debounceTimers = new Map();
    }

    /**
     * Valida RUT en tiempo real
     */
    async validarRut(rutInput, messageElement) {
        const rut = rutInput.value.trim();

        if (!rut) {
            this.mostrarMensaje(messageElement, '', 'neutral');
            return null;
        }

        try {
            const response = await fetch(`${this.baseUrl}/rut?rut=${encodeURIComponent(rut)}`);
            const result = await response.json();

            if (result.valido) {
                rutInput.value = result.valor; // RUT formateado
                this.mostrarMensaje(messageElement, result.mensaje, 'success');
            } else {
                this.mostrarMensaje(messageElement, result.mensaje, 'error');
            }

            return result;
        } catch (error) {
            console.error('Error validando RUT:', error);
            this.mostrarMensaje(messageElement, 'Error al validar RUT', 'error');
            return null;
        }
    }

    /**
     * Valida código de producto con debounce
     */
    validarCodigoProducto(codigoInput, messageElement, excludeId = null) {
        const codigo = codigoInput.value.trim();
        const timerId = 'codigo-' + (excludeId || 'new');

        // Limpiar timer anterior
        if (this.debounceTimers.has(timerId)) {
            clearTimeout(this.debounceTimers.get(timerId));
        }

        if (!codigo) {
            this.mostrarMensaje(messageElement, '', 'neutral');
            return;
        }

        // Debounce de 500ms
        const timer = setTimeout(async () => {
            try {
                let url = `${this.baseUrl}/producto/codigo?codigo=${encodeURIComponent(codigo)}`;
                if (excludeId) {
                    url += `&excludeId=${excludeId}`;
                }

                const response = await fetch(url);
                const result = await response.json();

                const messageType = result.valido ? 'success' : 'error';
                this.mostrarMensaje(messageElement, result.mensaje, messageType);

                // Formatear código en mayúsculas
                if (result.valido) {
                    codigoInput.value = result.valor;
                }

            } catch (error) {
                console.error('Error validando código:', error);
                this.mostrarMensaje(messageElement, 'Error al validar código', 'error');
            }
        }, 500);

        this.debounceTimers.set(timerId, timer);
    }

    /**
     * Valida email con debounce
     */
    validarEmail(emailInput, messageElement, excludeId = null) {
        const email = emailInput.value.trim();
        const timerId = 'email-' + (excludeId || 'new');

        if (this.debounceTimers.has(timerId)) {
            clearTimeout(this.debounceTimers.get(timerId));
        }

        if (!email) {
            this.mostrarMensaje(messageElement, '', 'neutral');
            return;
        }

        const timer = setTimeout(async () => {
            try {
                let url = `${this.baseUrl}/email?email=${encodeURIComponent(email)}`;
                if (excludeId) {
                    url += `&excludeId=${excludeId}`;
                }

                const response = await fetch(url);
                const result = await response.json();

                const messageType = result.valido ? 'success' : 'error';
                this.mostrarMensaje(messageElement, result.mensaje, messageType);

            } catch (error) {
                console.error('Error validando email:', error);
                this.mostrarMensaje(messageElement, 'Error al validar email', 'error');
            }
        }, 500);

        this.debounceTimers.set(timerId, timer);
    }

    /**
     * Valida stock antes de agregar producto a venta
     */
    async validarStock(productoId, cantidad) {
        try {
            const response = await fetch(`${this.baseUrl}/stock`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    productoId: productoId,
                    cantidad: cantidad
                })
            });

            return await response.json();
        } catch (error) {
            console.error('Error validando stock:', error);
            return { valido: false, mensaje: 'Error al validar stock' };
        }
    }

    /**
     * Valida rango de fechas para reportes
     */
    async validarRangoFechas(fechaInicio, fechaFin, messageElement) {
        if (!fechaInicio || !fechaFin) {
            this.mostrarMensaje(messageElement, '', 'neutral');
            return null;
        }

        try {
            const url = `${this.baseUrl}/fechas/rango?fechaInicio=${fechaInicio}&fechaFin=${fechaFin}`;
            const response = await fetch(url);
            const result = await response.json();

            const messageType = result.valido ? 'success' : 'error';
            this.mostrarMensaje(messageElement, result.mensaje, messageType);

            return result;
        } catch (error) {
            console.error('Error validando fechas:', error);
            this.mostrarMensaje(messageElement, 'Error al validar fechas', 'error');
            return null;
        }
    }

    /**
     * Validación completa de venta antes de enviar
     */
    async validarVentaCompleta(ventaData) {
        try {
            const response = await fetch(`${this.baseUrl}/venta/completa`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(ventaData)
            });

            return await response.json();
        } catch (error) {
            console.error('Error en validación completa:', error);
            return {
                valido: false,
                mensaje: 'Error en validación',
                errores: ['Error de conexión con el servidor']
            };
        }
    }

    /**
     * Configurar validaciones automáticas en un formulario
     */
    configurarFormulario(formElement, opciones = {}) {
        const defaults = {
            validarEnTiempoReal: true,
            mostrarIconos: true,
            excludeId: null
        };

        const config = { ...defaults, ...opciones };

        // RUT
        const rutInput = formElement.querySelector('[data-validar="rut"]');
        if (rutInput) {
            const messageEl = this.getOrCreateMessageElement(rutInput);

            rutInput.addEventListener('blur', () => {
                this.validarRut(rutInput, messageEl);
            });

            if (config.validarEnTiempoReal) {
                rutInput.addEventListener('input', () => {
                    // Solo validar si tiene al menos 8 caracteres
                    if (rutInput.value.length >= 8) {
                        this.validarRut(rutInput, messageEl);
                    }
                });
            }
        }

        // Código de producto
        const codigoInput = formElement.querySelector('[data-validar="codigo-producto"]');
        if (codigoInput) {
            const messageEl = this.getOrCreateMessageElement(codigoInput);

            codigoInput.addEventListener('input', () => {
                this.validarCodigoProducto(codigoInput, messageEl, config.excludeId);
            });
        }

        // Email
        const emailInput = formElement.querySelector('[data-validar="email"]');
        if (emailInput) {
            const messageEl = this.getOrCreateMessageElement(emailInput);

            emailInput.addEventListener('input', () => {
                this.validarEmail(emailInput, messageEl, config.excludeId);
            });
        }

        // Rango de fechas
        const fechaInicioInput = formElement.querySelector('[data-validar="fecha-inicio"]');
        const fechaFinInput = formElement.querySelector('[data-validar="fecha-fin"]');

        if (fechaInicioInput && fechaFinInput) {
            const messageEl = this.getOrCreateMessageElement(fechaFinInput);

            const validarRango = () => {
                if (fechaInicioInput.value && fechaFinInput.value) {
                    this.validarRangoFechas(fechaInicioInput.value, fechaFinInput.value, messageEl);
                }
            };

            fechaInicioInput.addEventListener('change', validarRango);
            fechaFinInput.addEventListener('change', validarRango);
        }
    }

    /**
     * Crea o obtiene elemento para mostrar mensajes de validación
     */
    getOrCreateMessageElement(input) {
        let messageEl = input.parentNode.querySelector('.validation-message');

        if (!messageEl) {
            messageEl = document.createElement('div');
            messageEl.className = 'validation-message';
            messageEl.style.fontSize = '0.875rem';
            messageEl.style.marginTop = '0.25rem';
            input.parentNode.appendChild(messageEl);
        }

        return messageEl;
    }

    /**
     * Muestra mensaje de validación con estilos
     */
    mostrarMensaje(element, message, type) {
        if (!element) return;

        element.textContent = message;
        element.className = 'validation-message';

        // Remover clases anteriores
        element.classList.remove('text-success', 'text-danger', 'text-muted');

        // Agregar clase según tipo
        switch (type) {
            case 'success':
                element.classList.add('text-success');
                break;
            case 'error':
                element.classList.add('text-danger');
                break;
            case 'neutral':
            default:
                element.classList.add('text-muted');
                break;
        }
    }

    /**
     * Método de utilidad para mostrar errores de validación en formularios
     */
    mostrarErroresFormulario(formElement, errores) {
        // Limpiar errores anteriores
        formElement.querySelectorAll('.validation-message').forEach(el => {
            el.textContent = '';
            el.className = 'validation-message text-muted';
        });

        // Mostrar nuevos errores
        errores.forEach(error => {
            console.warn('Error de validación:', error);

            // Aquí podrías implementar lógica para mapear errores específicos
            // a campos específicos basándose en el mensaje de error
        });

        // Mostrar resumen de errores
        const errorSummary = formElement.querySelector('.error-summary');
        if (errorSummary) {
            errorSummary.innerHTML = errores.map(error =>
                `<div class="alert alert-danger alert-sm">${error}</div>`
            ).join('');
        }
    }
}

// Instancia global para usar en las páginas
const validacionManager = new ValidacionManager();

// Configuración automática cuando el DOM esté listo
document.addEventListener('DOMContentLoaded', function() {
    // Buscar formularios con atributo data-validar-auto
    const formularios = document.querySelectorAll('form[data-validar-auto]');

    formularios.forEach(form => {
        const excludeId = form.dataset.excludeId || null;
        validacionManager.configurarFormulario(form, { excludeId });
    });
});

// Exportar para uso en módulos
if (typeof module !== 'undefined' && module.exports) {
    module.exports = ValidacionManager;
}