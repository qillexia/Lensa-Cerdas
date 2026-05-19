const express = require("express");
const router = express.Router();
const supabase = require("../config/db");
const { GoogleGenerativeAI } = require("@google/generative-ai");

// Initialize Gemini AI
const genAI = new GoogleGenerativeAI(process.env.GEMINI_API_KEY);
const model = genAI.getGenerativeModel({ model: "gemini-2.5-flash" });

// 1. POST /api/summarize: Create a summary
router.post("/summarize", async (req, res) => {
    const { user_id, title, content, style, length } = req.body;

    if (!user_id || !title || !content) {
        return res.status(400).json({ success: false, message: "User ID, title, and content are required" });
    }

    try {
        let styleInstruction = "secara padat dan jelas untuk mahasiswa";
        if (style === "santai") styleInstruction = "dengan gaya bahasa santai dan mudah dipahami anak muda";
        else if (style === "poin") styleInstruction = "dalam bentuk poin-poin penting (bullet points) yang terstruktur";
        
        let lengthInstruction = "";
        if (length === "singkat") lengthInstruction = "Buat ringkasan yang sangat singkat (maksimal 1-2 paragraf pendek).";
        else if (length === "detail") lengthInstruction = "Berikan penjelasan yang detail dan komprehensif.";

        const prompt = `Tolong ringkas materi kuliah berikut ini ${styleInstruction}. ${lengthInstruction} PENTING: Jangan gunakan format markdown (seperti tanda bintang *, hashtag #, atau teks tebal/miring). Berikan teks biasa (plain text) saja:\n\nJudul: ${title}\nKonten: ${content}`;
        const result = await model.generateContent(prompt);
        let summaryText = result.response.text();
        summaryText = summaryText.replace(/\*/g, '').replace(/#/g, '');

        const { data, error } = await supabase
            .from('summaries')
            .insert([{ user_id, title, original_text: content, summary_text: summaryText }])
            .select()
            .single();

        if (error) {
            console.error("DB Error:", error);
            return res.status(500).json({ success: false, message: "Gagal menyimpan ke database" });
        }

        res.json({
            success: true,
            message: "Ringkasan berhasil dibuat dan disimpan!",
            data: {
                id: data.id,
                title,
                summary: summaryText
            }
        });
    } catch (err) {
        console.error("Error:", err);
        res.status(500).json({ success: false, message: err.message });
    }
});

// 2. GET /api/history: Read records per user (with pagination)
router.get("/history", async (req, res) => {
    const { user_id, limit, offset } = req.query;

    if (!user_id) {
        return res.status(400).json({ success: false, message: "User ID is required" });
    }

    const pageLimit = Math.min(parseInt(limit) || 20, 50); // Max 50, default 20
    const pageOffset = parseInt(offset) || 0;

    const { data, error, count } = await supabase
        .from('summaries')
        .select('id, user_id, title, summary_text, created_at', { count: 'exact' })
        .eq('user_id', user_id)
        .order('created_at', { ascending: false })
        .range(pageOffset, pageOffset + pageLimit - 1);

    if (error) {
        return res.status(500).json({ success: false, message: error.message });
    }
    res.json({ success: true, data, total: count });
});

// 3. PUT /api/update/:id: Update an existing summary
router.put("/update/:id", async (req, res) => {
    const { title, summary_text } = req.body;
    const { id } = req.params;

    const { error } = await supabase
        .from('summaries')
        .update({ title, summary_text })
        .eq('id', id);

    if (error) {
        return res.status(500).json({ success: false, message: error.message });
    }
    res.json({ success: true, message: "Data berhasil diperbarui!" });
});

// 4. DELETE /api/delete/:id: Delete a record
router.delete("/delete/:id", async (req, res) => {
    const { id } = req.params;

    const { error } = await supabase
        .from('summaries')
        .delete()
        .eq('id', id);

    if (error) {
        return res.status(500).json({ success: false, message: error.message });
    }
    res.json({ success: true, message: "Data berhasil dihapus!" });
});

// 5. POST /api/login: Handle Google Login and save user
router.post("/login", async (req, res) => {
    const { google_id, name, email, photo_url } = req.body;

    if (!google_id || !email) {
        return res.status(400).json({ success: false, message: "Google ID and Email are required." });
    }

    // Check if user exists
    const { data: existingUser, error: checkError } = await supabase
        .from('users')
        .select('*')
        .eq('email', email)
        .single();

    if (existingUser) {
        // User exists, update last_login
        await supabase
            .from('users')
            .update({ last_login: new Date().toISOString() })
            .eq('email', email);
        return res.json({ success: true, message: "Login successful", data: existingUser });
    }

    // New user, insert into DB
    const { data: newUser, error: insertError } = await supabase
        .from('users')
        .insert([{ google_id, name, email, photo_url }])
        .select()
        .single();

    if (insertError) {
        return res.status(500).json({ success: false, message: insertError.message });
    }

    res.json({
        success: true,
        message: "User registered and logged in",
        data: newUser
    });
});

// 6. GET /api/test: Test connection
router.get("/test", async (req, res) => {
    const { data, error } = await supabase.from('users').select('count', { count: 'exact', head: true });
    res.status(200).json({
        success: !error,
        message: error ? error.message : "Koneksi ke Backend LensaCerdas + Supabase Berhasil!"
    });
});

module.exports = router;
