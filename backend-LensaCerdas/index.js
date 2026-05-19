require('dotenv').config();

// 1. Import Library
const express = require('express');
const path = require('path');

// 2. Konfigurasi Awal
const app = express();
const PORT = process.env.PORT || 3000;
const supabase = require('./config/db');
const apiRoutes = require('./routes/api');

// 3. Middleware
app.use(express.json({ limit: '50mb' }));
app.use(express.urlencoded({ limit: '50mb', extended: true }));
app.use(express.static(path.join(__dirname, 'public')));

// 4. Setup View Engine (EJS) untuk Tampilan Web
app.set('view engine', 'ejs');
app.set('views', path.join(__dirname, 'views'));

// 5. Rute API (Endpoint REST API untuk Android)
app.use('/api', apiRoutes);

// 6. Rute untuk Web Admin (Tampilan di Browser)
app.get('/', async (req, res) => {
    const { data, error } = await supabase
        .from('summaries')
        .select('*')
        .order('created_at', { ascending: false });

    if (error) {
        return res.status(500).send("Database Error: " + error.message);
    }
    res.render('index', { 
        title: 'Admin LensaCerdas',
        summaries: data || []
    });
});

// 7. Menjalankan Server (hanya untuk lokal, Vercel tidak perlu ini)
if (process.env.NODE_ENV !== 'production') {
    app.listen(PORT, () => {
        console.log(`Server LensaCerdas jalan di http://localhost:${PORT}`);
    });
}

module.exports = app;