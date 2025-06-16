document.addEventListener('DOMContentLoaded', function () {
    // Función para probar el envío de correos
    const btnProbarCorreo = document.getElementById('btnProbarCorreo');
    const emailPrueba = document.getElementById('emailPrueba');

    if (btnProbarCorreo) {
        btnProbarCorreo.addEventListener('click', function () {
            const email = emailPrueba.value.trim();

            if (!email) {
                alert('Por favor, ingrese un correo electrónico para la prueba');
                return;
            }

            // Verificar formato de email básico
            const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
            if (!emailRegex.test(email)) {
                alert('Por favor, ingrese un correo electrónico válido');
                return;
            }

            // Mostrar indicador de carga
            btnProbarCorreo.disabled = true;
            btnProbarCorreo.innerHTML = '<i class="fas fa-spinner fa-spin me-1"></i> Enviando...';

            // Enviar solicitud AJAX
            fetch('/admin/configuracion/probar-correo?email=' + encodeURIComponent(email), {
                method: 'POST'
            })
                .then(response => response.text())
                .then(data => {
                    alert(data);
                })
                .catch(error => {
                    alert('Error: ' + error);
                })
                .finally(() => {
                    // Restaurar botón
                    btnProbarCorreo.disabled = false;
                    btnProbarCorreo.innerHTML = '<i class="fas fa-paper-plane me-1"></i> Probar';
                });
        });
    }

    // Sincronizar inputs de color
    const colorPicker = document.querySelector('input[type="color"]');
    const colorText = colorPicker.nextElementSibling;

    colorPicker.addEventListener('input', function () {
        colorText.value = this.value;
    });

    colorText.addEventListener('input', function () {
        // Validar formato de color hexadecimal
        if (/^#[0-9A-F]{6}$/i.test(this.value)) {
            colorPicker.value = this.value;
        }
    });
});