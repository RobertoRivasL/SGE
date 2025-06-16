document.addEventListener('DOMContentLoaded', function () {
    const formularioLogin = document.getElementById('loginForm');
    const inputUsuario = document.getElementById('username');
    const inputContraseña = document.getElementById('password');
    const botonMostrarContraseña = document.querySelector('.toggle-password');
    const mensajeError = document.getElementById('errorMessage');

    // Iconos predefinidos para evitar recreaciones constantes
    const iconoPasswordOculto = '<i class="fas fa-eye"></i>';
    const iconoPasswordMostrado = '<i class="fas fa-eye-slash"></i>';

    // Función para mostrar/ocultar la contraseña
    botonMostrarContraseña.addEventListener('click', function () {
        if (inputContraseña.type === "password") {
            inputContraseña.type = "text";
            botonMostrarContraseña.innerHTML = iconoPasswordMostrado;
        } else {
            inputContraseña.type = "password";
            botonMostrarContraseña.innerHTML = iconoPasswordOculto;
        }
    });

    // Manejo del formulario de login
    formularioLogin.addEventListener('submit', async function (evento) {
        evento.preventDefault();

        const usuario = inputUsuario.value.trim();
        const contraseña = inputContraseña.value.trim();

        // Validación: Si los campos están vacíos, mostramos un error
        if (!usuario || !contraseña) {
            mensajeError.textContent = 'Por favor, ingrese ambos campos (usuario y contraseña).';
            mensajeError.style.display = 'block';
            return;
        }

        // Ocultar el mensaje de error si los campos son correctos
        mensajeError.style.display = 'none';

        const loginData = {
            username: usuario,
            password: contraseña
        };

        // --- NUEVO: Obtener el token CSRF del campo oculto ---
        const csrfTokenElement = formularioLogin.querySelector('input[name="_csrf"]');
        let csrfToken = null;
        if (csrfTokenElement) {
            csrfToken = csrfTokenElement.value;
        }
        // ----------------------------------------------------

        const headers = {
            'Content-Type': 'application/x-www-form-urlencoded',
            // --- NUEVO: Añadir el token CSRF a los encabezados si está disponible ---
            // Spring Security espera la cabecera 'X-CSRF-TOKEN' por defecto para peticiones AJAX
            ...(csrfToken && {'X-CSRF-TOKEN': csrfToken})
            // --------------------------------------------------------------------
        };

        try {
            const respuesta = await fetch(formularioLogin.action, {
                method: 'POST',
                headers: headers, // Usar los encabezados actualizados
                body: new URLSearchParams(loginData),
            });

            // Manejar las respuestas del servidor (200 OK para éxito, otros para error)
            if (respuesta.ok) { // respuesta.ok es true para estados 200-299
                // Si el login es exitoso, redirigir a la página de dashboard
                window.location.href = '/inicio'; // O la URL de éxito configurada en Spring Security
            } else if (respuesta.status === 401) { // No autorizado (credenciales inválidas)
                mensajeError.textContent = 'Usuario o contraseña incorrectos.';
                mensajeError.style.display = 'block';
            } else {
                // Otro tipo de error del servidor (ej. 403 Forbidden, 500 Internal Server Error)
                // Podríamos intentar leer el cuerpo de la respuesta si el servidor proporciona más detalles
                // const errorText = await respuesta.text(); // O respuesta.json() si devuelve JSON
                mensajeError.textContent = `Error en el inicio de sesión. Código de estado: ${respuesta.status}`;
                mensajeError.style.display = 'block';
            }

        } catch (error) {
            // Error de red o error antes de recibir respuesta del servidor
            console.error('Error al enviar la solicitud de login:', error);
            mensajeError.textContent = 'Hubo un error al intentar iniciar sesión. Por favor, inténtelo de nuevo más tarde.';
            mensajeError.style.display = 'block';
        }
    });
});