import { defineConfig } from 'vitepress'
import { withMermaid } from 'vitepress-plugin-mermaid'

const github = 'https://github.com/kaleidot725/PulseMVI'

export default withMermaid(defineConfig({
  title: 'PulseMVI',
  base: '/PulseMVI/',

  head: [
    ['link', { rel: 'icon', type: 'image/svg+xml', href: '/PulseMVI/favicon.svg' }],
  ],

  locales: {
    root: {
      label: 'English',
      lang: 'en',
      description: 'A lightweight MVI library for Compose Desktop',
      themeConfig: {
        nav: [
          { text: 'Guide', link: '/guide/', activeMatch: '/guide/' },
          { text: 'API', link: '/api/', activeMatch: '/api/' },
          { text: 'Changelog', link: `${github}/releases` },
        ],
        sidebar: {
          '/guide/': [
            {
              text: 'Introduction',
              items: [
                { text: 'What is PulseMVI?', link: '/guide/' },
                { text: 'Getting Started', link: '/guide/getting-started' },
              ],
            },
            {
              text: 'Core Concepts',
              items: [
                { text: 'Architecture', link: '/guide/architecture' },
                { text: 'ViewModel', link: '/guide/viewmodel' },
                { text: 'Container', link: '/guide/container' },
                { text: 'Broadcast', link: '/guide/broadcast' },
                { text: 'Unicast', link: '/guide/unicast' },
              ],
            },
            {
              text: 'Extensions',
              items: [
                { text: 'Navigation 3', link: '/guide/navigation3' },
              ],
            },
          ],
          '/api/': [
            {
              text: 'API Reference',
              items: [
                { text: 'Overview', link: '/api/' },
                { text: 'PulseViewModel', link: '/api/pulse-viewmodel' },
                { text: 'PulseContainer', link: '/api/pulse-container' },
                { text: 'Composables', link: '/api/composables' },
                { text: 'Marker Interfaces', link: '/api/interfaces' },
              ],
            },
            {
              text: 'Extensions',
              items: [
                { text: 'Navigation 3', link: '/api/navigation3' },
              ],
            },
          ],
        },
        editLink: {
          pattern: `${github}/edit/main/docs/:path`,
          text: 'Edit this page on GitHub',
        },
        footer: {
          message: 'Released under the Apache 2.0 License.',
          copyright: 'Copyright © 2026 kaleidot725',
        },
      },
    },
    ja: {
      label: '日本語',
      lang: 'ja',
      link: '/ja/',
      description: 'Compose Desktop 向けの軽量 MVI ライブラリ',
      themeConfig: {
        nav: [
          { text: 'ガイド', link: '/ja/guide/', activeMatch: '/ja/guide/' },
          { text: 'API', link: '/ja/api/', activeMatch: '/ja/api/' },
          { text: '変更履歴', link: `${github}/releases` },
        ],
        sidebar: {
          '/ja/guide/': [
            {
              text: 'はじめに',
              items: [
                { text: 'PulseMVI とは', link: '/ja/guide/' },
                { text: 'はじめかた', link: '/ja/guide/getting-started' },
              ],
            },
            {
              text: 'コアコンセプト',
              items: [
                { text: 'アーキテクチャ', link: '/ja/guide/architecture' },
                { text: 'ViewModel', link: '/ja/guide/viewmodel' },
                { text: 'Container', link: '/ja/guide/container' },
                { text: 'Broadcast', link: '/ja/guide/broadcast' },
                { text: 'Unicast', link: '/ja/guide/unicast' },
              ],
            },
            {
              text: '拡張',
              items: [
                { text: 'Navigation 3', link: '/ja/guide/navigation3' },
              ],
            },
          ],
          '/ja/api/': [
            {
              text: 'API リファレンス',
              items: [
                { text: '概要', link: '/ja/api/' },
                { text: 'PulseViewModel', link: '/ja/api/pulse-viewmodel' },
                { text: 'PulseContainer', link: '/ja/api/pulse-container' },
                { text: 'Composable', link: '/ja/api/composables' },
                { text: 'マーカーインターフェース', link: '/ja/api/interfaces' },
              ],
            },
            {
              text: '拡張',
              items: [
                { text: 'Navigation 3', link: '/ja/api/navigation3' },
              ],
            },
          ],
        },
        editLink: {
          pattern: `${github}/edit/main/docs/:path`,
          text: 'GitHub でこのページを編集',
        },
        footer: {
          message: 'Apache 2.0 License の下で公開されています。',
          copyright: 'Copyright © 2026 kaleidot725',
        },
        outline: { label: 'このページの内容' },
        docFooter: { prev: '前のページ', next: '次のページ' },
        lastUpdated: { text: '最終更新' },
        returnToTopLabel: 'トップへ戻る',
        sidebarMenuLabel: 'メニュー',
        darkModeSwitchLabel: 'テーマ',
        langMenuLabel: '言語を変更',
      },
    },
  },

  mermaid: {
    theme: 'base',
    themeVariables: {
      fontFamily: 'Inter, ui-sans-serif, system-ui, sans-serif',
      fontSize: '14px',
    },
  },

  themeConfig: {
    logo: '/logo.svg',

    socialLinks: [
      { icon: 'github', link: github },
    ],

    search: {
      provider: 'local',
      options: {
        locales: {
          ja: {
            translations: {
              button: { buttonText: '検索', buttonAriaLabel: '検索' },
              modal: {
                noResultsText: '見つかりませんでした',
                resetButtonTitle: 'クリア',
                footer: { selectText: '選択', navigateText: '移動', closeText: '閉じる' },
              },
            },
          },
        },
      },
    },
  },
}))
