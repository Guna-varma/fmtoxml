# 🎯 How to Download ZIP File in Postman - Step by Step Guide

## ⚠️ The Problem

When you call `/api/transform/{jobId}/result`, you see:
- Binary characters: `PK`, `NUL`, control characters
- Unreadable text
- File paths like `xml/topic_001.xml` mixed with garbage

**This is NORMAL!** The ZIP file is being created correctly, but Postman is trying to **display** it as text instead of **downloading** it.

---

## ✅ Solution: Use "Send and Download"

### Method 1: Send and Download (EASIEST) ⭐

1. **In Postman**, look at the **Send** button
2. **Click the DROPDOWN ARROW** next to "Send" (▼)
3. **Select "Send and Download"** from the dropdown menu
4. Postman will automatically prompt you to save the file
5. **Save it** as `transformation_{jobId}.zip`
6. Done! ✅

**Visual Guide:**
```
[Send ▼]  ← Click the arrow!
  ├─ Send
  └─ Send and Download  ← Select this!
```

---

### Method 2: Save Response After Sending

1. Click the regular **Send** button
2. Wait for the response (you'll see binary data - that's OK!)
3. In the **response section**, look for the **three dots (...)** menu
4. Click the **three dots (...)**
5. Select **"Save Response"**
6. Select **"Save to a file"**
7. Choose where to save and name it `transformation_{jobId}.zip`

---

## 🔍 How to Verify It Worked

After downloading:

1. **Check the file:**
   - Filename: `transformation_{jobId}.zip`
   - File size: Should be > 0 bytes (usually several KB)

2. **Open the ZIP file:**
   - Double-click to open (Windows/Mac can open ZIP files natively)
   - You should see:
     ```
     output/
     ├── main.ditamap
     ├── table-of-contents/
     │   └── toc.xml
     ├── xml/
     │   ├── topic_001.xml
     │   ├── topic_002.xml
     │   └── ...
     ├── images/
     │   └── *.eps (if any)
     └── chapters/
         ├── chapter_01.ditamap
         └── chapter_02.ditamap
     ```

---

## 🚨 Why You See Binary Data

**The binary data is CORRECT!**

- `PK` = ZIP file signature (every ZIP file starts with this)
- `NUL` = Null bytes (normal in binary files)
- Control characters = Normal binary data
- `xml/topic_001.xml` = File paths inside the ZIP

**This proves the ZIP file is being created correctly!**

Postman just needs to be told to **download** it instead of **displaying** it as text.

---

## 📋 Complete Workflow

1. **Upload file** → Get `jobId`
2. **Check status** → Wait until `COMPLETED`
3. **Download result** → Use "Send and Download" button
4. **Extract ZIP** → View your transformed DITA files

---

## 🔧 Alternative: Use cURL (If Postman Still Doesn't Work)

```bash
curl -O -J http://localhost:8080/api/transform/{jobId}/result
```

This automatically downloads the file with the correct name.

---

## ❓ Troubleshooting

**Q: I still see binary data after using "Send and Download"**
- A: Check if the file was saved in your Downloads folder
- A: Look for a download notification in Postman

**Q: The file is empty (0 bytes)**
- A: Make sure the job status is `COMPLETED` first
- A: Check the job details endpoint to verify output path exists

**Q: Getting 404 error**
- A: Job ID might be wrong - use the exact ID from upload response
- A: Transformation might not be completed yet - check status first

---

## ✅ Quick Checklist

- [ ] Job status is `COMPLETED` (use endpoint #2)
- [ ] Used "Send and Download" button (not regular Send)
- [ ] File saved with `.zip` extension
- [ ] File size > 0 bytes
- [ ] Can open and see DITA files inside

**If all checked ✅, you're done!**

