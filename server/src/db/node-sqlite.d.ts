declare module 'node:sqlite' {
  export class Database {
    constructor(filename: string | Buffer);
    exec(sql: string): void;
    prepare(sql: string): Statement;
    close(): void;
  }

  export class Statement {
    run(...params: any[]): { changes: number; lastInsertRowid: number };
    get(...params: any[]): any;
    all(...params: any[]): any[];
  }
}
