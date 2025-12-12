# How to Download ZIP File in Postman

## Issue
When calling the `/api/transform/{jobId}/result` endpoint, you see "PK" characters instead of downloading the ZIP file.

This happens because **Postman is trying to display the binary ZIP file as text** instead of downloading it.

## Solution: Use "Send and Download" in Postman

### Method 1: Use "Send and Download" Button (RECOMMENDED)

1. **In Postman**, for the "Get Transformation Result" request:
   - Look at the **Send** button
   - Click the **dropdown arrow** next to "Send"
   - Select **"Send and Download"** from the dropdown
   - Click it

2. Postman will prompt you to save the file
3. Save it with the name: `transformation_{jobId}.zip`
4. The ZIP file will be saved to your Downloads folder

### Method 2: Use "Save Response" Option

1. After clicking **Send**, wait for the response
2. In the response section, click the **three dots (...)** menu
3. Select **"Save Response"** → **"Save to a file"**
4. Choose where to save and name it `transformation_{jobId}.zip`

### Method 3: Verify Response Headers

The response should have these headers:
```
Content-Type: application/octet-stream
Content-Disposition: attachment; filename="transformation_{jobId}.zip"
```

If you see these headers, the server is sending the file correctly.

## What's in the ZIP File?

Once downloaded, the ZIP contains:
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

## Testing with cURL (Alternative)

If Postman continues to have issues, use cURL:

```bash
curl -O -J http://localhost:8080/api/transform/{jobId}/result
```

The `-O` flag saves the file, and `-J` uses the filename from Content-Disposition header.

