import { resolve } from 'path';
import { defineConfig } from 'vite';
import vue from '@vitejs/plugin-vue';
import vueJsx from '@vitejs/plugin-vue-jsx';
import svgLoader from 'vite-svg-loader';
import configArcoStyleImportPlugin from './plugin/arcoStyleImport';

export default defineConfig({
  base: '/seems/',
  server: {
    proxy: {
      '/SEEMSAPI': {
        target: 'http://127.0.0.1:8080',
        rewrite: (path) => path.replace(/^\/SEEMSAPI/, ''),
        changeOrigin: true,
      },
    },
  },
  plugins: [
    vue({
      template: {
        compilerOptions: {
          isCustomElement: (tag) =>
            [
              'primitive',
              'TresPerspectiveCamera',
              'TresPlaneGeometry',
              'TresAmbientLight',
              'TresDirectionalLight',
              'TresMesh',
              'TresMeshBasicMaterial',
            ].includes(tag),
        },
      },
    }),
    vueJsx(),
    svgLoader({ svgoConfig: {} }),
    configArcoStyleImportPlugin(),
  ],
  resolve: {
    alias: [
      {
        find: '@',
        replacement: resolve(__dirname, '../src'),
      },
      {
        find: 'assets',
        replacement: resolve(__dirname, '../src/assets'),
      },
      {
        find: 'vue-i18n',
        replacement: 'vue-i18n/dist/vue-i18n.cjs.js', // Resolve the i18n warning issue
      },
      {
        find: 'vue',
        replacement: 'vue/dist/vue.esm-bundler.js', // compile template
      },
    ],
    extensions: ['.ts', '.js'],
  },
  define: {
    'process.env': {},
    '__VUE_PROD_HYDRATION_MISMATCH_DETAILS__': true,
    '_AMapSecurityConfig': {
      securityJsCode: '7fa52993ea02e33132f819abe91b70a6',
    },
  },
  css: {
    preprocessorOptions: {
      less: {
        modifyVars: {
          hack: `true; @import (reference) "${resolve(
            'src/assets/style/breakpoint.less'
          )}";`,
        },
        javascriptEnabled: true,
      },
    },
  },
});
