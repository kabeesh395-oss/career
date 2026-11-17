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

const MIME_TO_EXTENSION: Record<string, string> = {
  'application/pdf': '.pdf',
  'application/x-pdf': '.pdf',
  'application/acrobat': '.pdf',
  'text/plain': '.txt',
  'application/vnd.openxmlformats-officedocument.wordprocessingml.document': '.docx',
  'application/zip': '.docx',
  'application/x-zip-compressed': '.docx',
  'application/msword': '.doc'
};

const ALLOWED_EXTENSIONS = new Set(['.pdf', '.txt', '.docx']);

const fileFilter = (req: any, file: Express.Multer.File, cb: multer.FileFilterCallback) => {
  const ext = path.extname(file.originalname).toLowerCase();
  const declaredExtension = MIME_TO_EXTENSION[file.mimetype];

  if (!ALLOWED_EXTENSIONS.has(ext)) {
    cb(new Error('Invalid file format. Only PDF, TXT, and DOCX files are supported.'));
    return;
  }

  if (declaredExtension && declaredExtension !== ext) {
    cb(new Error('File extension does not match the declared file type.'));
    return;
  }

  // Unknown/generic MIME values are checked against the file signature below.
  cb(null, true);
};

export const uploadResume = multer({
  storage,
  fileFilter,
  limits: {
    fileSize: 10 * 1024 * 1024 // 10MB limit
  }
});

/**
 * Validate the file bytes after Multer has written the upload. Client-provided
 * MIME types and extensions are not trusted as parser-safety controls.
 */
export async function validateResumeFile(file: Express.Multer.File): Promise<void> {
  const ext = path.extname(file.originalname).toLowerCase();
  if (!ALLOWED_EXTENSIONS.has(ext)) {
    throw new Error('Only PDF, TXT, and DOCX files are supported.');
  }

  const declaredExtension = MIME_TO_EXTENSION[file.mimetype];
  if (declaredExtension && declaredExtension !== ext) {
    throw new Error('The uploaded file extension does not match its declared MIME type.');
  }

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
