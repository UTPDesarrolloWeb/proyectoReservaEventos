const http = require('http');

async function test() {
    // Register User
    const registerBody = JSON.stringify({
        nombre: 'Test',
        apellido: 'Test',
        email: 'test' + Date.now() + '@test.com',
        password: 'password',
        rol: 'ORGANIZADOR'
    });

    const res = await fetch('http://localhost:8080/api/auth/register', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: registerBody
    });
    const authData = await res.json();
    console.log('Auth Data:', authData);

    if (!authData.token) {
        console.log('Register failed');
        return;
    }

    // Create Event
    const eventBody = JSON.stringify({
        titulo: 'Test Event',
        descripcion: 'Desc',
        fechaEvento: '2026-10-10T10:00:00',
        lugar: 'Lugar',
        aforo: 100,
        precio: 50,
        categoria: 'OTRO'
    });

    const res2 = await fetch('http://localhost:8080/api/eventos', {
        method: 'POST',
        headers: { 
            'Content-Type': 'application/json',
            'Authorization': 'Bearer ' + authData.token
        },
        body: eventBody
    });
    
    const eventData = await res2.json();
    console.log('Event Response Status:', res2.status);
    console.log('Event Data:', eventData);
}
test();
