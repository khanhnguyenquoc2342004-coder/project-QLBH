import js from '@eslint/js';
import pluginVue from 'eslint-plugin-vue';
import globals from 'globals';

export default [
  { ignores: ['dist/**', 'node_modules/**'] },
  js.configs.recommended,
  ...pluginVue.configs['flat/recommended'],
  {
    files: ['src/**/*.{js,vue}'],
    languageOptions: {
      globals: globals.browser
    },
    rules: {
      'no-console': 'off',
      'no-empty': 'off',
      'no-unused-vars': 'off',
      'vue/max-attributes-per-line': 'off',
      'vue/singleline-html-element-content-newline': 'off',
      'vue/attributes-order': 'off',
      'vue/html-closing-bracket-spacing': 'off',
      'vue/v-on-event-hyphenation': 'off',
      'vue/multi-word-component-names': 'off',
      'vue/require-default-prop': 'off'
    }
  }
];
