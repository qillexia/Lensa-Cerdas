require('dotenv').config();

// 1. Import Library
const express = require('express');
const path = require('path');
const cookieParser = require('cookie-parser');

// 2. Konfigurasi Awal
const app = express();
const PORT = process.env.PORT || 3000;
const supabase = require('./config/db');
const apiRoutes = require('./routes/api');

// 3. Middleware
app.use(express.json({ limit: '50mb' }));
app.use(express.urlencoded({ limit: '50mb', extended: true }));
app.use(cookieParser());
app.use(express.static(path.join(__dirname, 'public')));

// Middleware Auth untuk Admin Web
const authAdmin = (req, res, next) => {
    if (req.cookies.admin_auth === 'true') {
        next();
    } else {
        res.redirect('/login');
    }
};

// Fungsi helper untuk mendapatkan/membuat dummy user Admin
const getOrCreateAdminUser = async () => {
    const adminEmail = 'admin@lensacerdas.local';
    const { data: existingAdmin } = await supabase
        .from('users')
        .select('id')
        .eq('email', adminEmail)
        .single();

    if (existingAdmin) return existingAdmin.id;

    // Buat baru jika belum ada
    const { data: newAdmin, error } = await supabase
        .from('users')
        .insert([{
            google_id: 'admin_dashboard',
            email: adminEmail,
            name: 'Admin Dashboard',
            photo_url: ''
        }])
        .select()
        .single();

    if (error) {
        console.error("Gagal membuat user Admin:", error);
        return null;
    }
    return newAdmin.id;
};

// 4. Setup View Engine (EJS) untuk Tampilan Web
app.set('view engine', 'ejs');
app.set('views', path.join(__dirname, 'views'));

// 5. Rute API (Endpoint REST API untuk Android)
app.use('/api', apiRoutes);

// 6. Rute Auth Web Admin
app.get('/login', (req, res) => {
    if (req.cookies.admin_auth === 'true') return res.redirect('/');
    res.render('login', { error: null });
});

app.post('/login', (req, res) => {
    const { username, password } = req.body;
    if (username === 'admin' && password === 'admin123') {
        res.cookie('admin_auth', 'true', { maxAge: 24 * 60 * 60 * 1000, httpOnly: true });
        res.redirect('/');
    } else {
        res.render('login', { error: 'Username atau Password salah!' });
    }
});

app.get('/logout', (req, res) => {
    res.clearCookie('admin_auth');
    res.redirect('/login');
});

// 7. Rute untuk Web Admin (Tampilan di Browser)
app.get('/', authAdmin, async (req, res) => {
    const { data, error } = await supabase
        .from('summaries')
        .select('*, users(name)')
        .order('created_at', { ascending: false });

    if (error) {
        return res.status(500).send("Database Error: " + error.message);
    }
    res.render('index', {
        title: 'Admin LensaCerdas',
        summaries: data || []
    });
});

// 8. Rute CRUD Khusus Web Admin (Create & Update)
app.post('/admin/summarize', authAdmin, async (req, res) => {
    const { title, summary_text } = req.body;
    if (!title || !summary_text) {
        return res.status(400).json({ success: false, message: "Title dan Summary harus diisi" });
    }

    const adminId = await getOrCreateAdminUser();
    if (!adminId) return res.status(500).json({ success: false, message: "Gagal inisialisasi akun Admin" });

    const { data, error } = await supabase
        .from('summaries')
        .insert([{
            user_id: adminId,
            title,
            original_text: "Created manually by Admin",
            summary_text
        }]);

    if (error) return res.status(500).json({ success: false, message: error.message });
    res.json({ success: true, message: "Ringkasan berhasil ditambahkan!" });
});

app.put('/admin/update/:id', authAdmin, async (req, res) => {
    const { title, summary_text } = req.body;
    const { id } = req.params;

    const { error } = await supabase
        .from('summaries')
        .update({ title, summary_text })
        .eq('id', id);

    if (error) return res.status(500).json({ success: false, message: error.message });
    res.json({ success: true, message: "Data berhasil diperbarui!" });
});

// 7. Menjalankan Server (hanya untuk lokal, Vercel tidak perlu ini)
if (process.env.NODE_ENV !== 'production') {
    app.listen(PORT, () => {
        console.log(`Server LensaCerdas jalan di http://localhost:${PORT}`);
    });
}

module.exports = app;