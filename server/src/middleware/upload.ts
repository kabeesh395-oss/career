import multer from 'multer';
import path from 'path';
import fs from 'fs';
import { fileURLToPath } from 'url';
import crypto from 'crypto';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

const UPLOAD_DIR = path.resolve(__dirname, '../../uploads/resumes');

if (!fs.existsSync(UPLOAD_DIR)) {
  fs.mkdirSync(UPLOAD_DIR, { recursive: true });
}

const storage = multer.diskStorage({
  destination: (req, file, cb) => {
    cb(null, UPLOAD_DIR);
  },
  filename: (req, file, cb) => {
    const ext = path.extname(file.originalname).toLowerCase();
    const uniqueName = `${Date.now()}-${crypto.randomBytes(8).toString('hex')}${ext}`;
    cb(null, uniqueName);
  }
});

const fileFilter = (req: any, file: Express.Multer.File, cb: multer.FileFilterCallback) => {
  const allowedExtensions = ['.pdf', '.txt', '.doc', '.docx'];
  const ext = path.extname(file.originalname).toLowerCase();

  if (allowedExtensions.includes(ext)) {
    cb(null, true);
  } else {
    cb(new Error('Invalid file format. Only PDF, TXT, and DOCX files are supported.'));
  }
};

export const uploadResume = multer({
  storage,
  fileFilter,
  limits: {
    fileSize: 10 * 1024 * 1024 // 10MB limit
  }
});

/**
 * Validate the bytes after Multer has written the file. Client-provided MIME
 * types and extensions are not trustworthy and are insufficient for parser
 * safety on their own.
 */
export async function validateResumeFile(file: Express.Multer.File): Promise<void> {
  const ext = path.extname(file.originalname).toLowerCase();
  const handle = await fs.promises.open(file.path, 'r');

  try {
    const header = Buffer.alloc(8);
    const { bytesRead } = await handle.read(header, 0, header.length, 0);
    const signature = header.subarray(0, bytesRead);

    if (ext === '.pdf' && signature.subarray(0, 5).toString('ascii') !== '%PDF-') {
      throw new Error('The uploaded PDF signature is invalid.');
    }

    if (ext === '.docx' && !(signature[0] === 0x50 && signature[1] === 0x4b && signature[2] === 0x03 && signature[3] === 0x04)) {
      throw new Error('The uploaded DOCX signature is invalid.');
    }

    if (ext === '.doc') {
      throw new Error('Legacy binary .DOC files are not supported. Please upload PDF, DOCX, or TXT.');
    }

    if (ext === '.txt') {
      const sample = await fs.promises.readFile(file.path);
      if (sample.includes(0)) {
        throw new Error('The uploaded text file contains binary data.');
      }
    }
  } finally {
    await handle.close();
  }
}
