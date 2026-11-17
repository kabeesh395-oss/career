import assert from 'node:assert/strict';
import fs from 'node:fs/promises';
import os from 'node:os';
import path from 'node:path';
import { validateResumeFile } from '../src/middleware/upload.js';

async function runUploadValidationTests() {
  const tempDir = await fs.mkdtemp(path.join(os.tmpdir(), 'career-resume-validation-'));

  const makeFile = async (name: string, mimetype: string, bytes: Buffer) => {
    const filePath = path.join(tempDir, name);
    await fs.writeFile(filePath, bytes);
    return { originalname: name, mimetype, path: filePath } as Express.Multer.File;
  };

  try {
    const pdfBytes = Buffer.from('%PDF-1.7\n1 0 obj\n<< /Type /Catalog >>\nendobj\n%%EOF\n');
    const validPdf = await makeFile('resume.pdf', 'application/pdf', pdfBytes);
    await validateResumeFile(validPdf);

    const genericMimePdf = await makeFile('generic.pdf', 'application/octet-stream', pdfBytes);
    await validateResumeFile(genericMimePdf);

    const validText = await makeFile('resume.txt', 'text/plain', Buffer.from('Resume text without binary data.'));
    await validateResumeFile(validText);

    const docxBytes = Buffer.from([0x50, 0x4b, 0x03, 0x04, 0x14, 0x00, 0x00, 0x00]);
    const validDocx = await makeFile(
      'resume.docx',
      'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
      docxBytes
    );
    await validateResumeFile(validDocx);

    const pdfNamedAsText = await makeFile('mislabeled.txt', 'application/pdf', pdfBytes);
    await assert.rejects(
      () => validateResumeFile(pdfNamedAsText),
      /extension does not match its declared MIME type/
    );

    const textMimePdf = await makeFile('mismatched.pdf', 'text/plain', pdfBytes);
    await assert.rejects(
      () => validateResumeFile(textMimePdf),
      /extension does not match its declared MIME type/
    );

    const unsupportedExtension = await makeFile('resume.bin', 'application/pdf', pdfBytes);
    await assert.rejects(
      () => validateResumeFile(unsupportedExtension),
      /Only PDF, TXT, and DOCX files are supported/
    );

    console.log('Resume upload validation tests passed: supported types, generic MIME fallback, mismatched MIME/extension, and unsupported extensions.');
  } finally {
    await fs.rm(tempDir, { recursive: true, force: true });
  }
}

runUploadValidationTests().catch((error) => {
  console.error('Resume upload validation tests failed:', error);
  process.exitCode = 1;
});
