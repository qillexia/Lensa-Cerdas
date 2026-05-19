require("dotenv").config();
const { GoogleGenerativeAI } = require("@google/generative-ai");

async function run() {
  const genAI = new GoogleGenerativeAI(process.env.GEMINI_API_KEY);
  console.log("Fetching models...");
  try {
    // Actually ModelService.ListModels is not exposed directly in this simple SDK
    // But we can try to fetch a well-known model or print error details
    console.log("Key being used:", process.env.GEMINI_API_KEY ? "EXISTS" : "MISSING");
    
    // Instead of listing models, let's try the newest standard names
    const modelsToTry = ["gemini-1.5-flash", "gemini-1.5-pro", "gemini-1.0-pro"];
    
    for (const m of modelsToTry) {
        try {
            console.log(`Trying ${m}...`);
            const model = genAI.getGenerativeModel({ model: m });
            const result = await model.generateContent("Test");
            console.log(`SUCCESS with ${m}:`, result.response.text());
            return;
        } catch(e) {
            console.log(`FAILED ${m}:`, e.message);
        }
    }
  } catch (err) {
    console.error("Error:", err);
  }
}

run();
