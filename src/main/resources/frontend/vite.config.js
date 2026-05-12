import { resolve } from 'path';
import { defineConfig } from 'vite';

export default defineConfig({
    build: {
        outDir: resolve(__dirname, '../static'),
        emptyOutDir: false,
        cssCodeSplit: false,
        manifest: false,
        sourcemap: false,
        rollupOptions: {
            input: resolve(__dirname, 'src/assets/js/main.js'),
            output: {
                entryFileNames: 'assets/js/main.js',
                assetFileNames: (assetInfo) => {
                    if (assetInfo.name && assetInfo.name.endsWith('.css')) {
                        return 'assets/css/main.css';
                    }

                    return 'assets/[name][extname]';
                }
            }
        }
    }
});