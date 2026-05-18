import { defineConfig } from 'vitepress'

export default defineConfig({
  title: 'PulseMVI',
  description: 'A lightweight MVI library for Compose Desktop',
  base: '/PulseMVI/',

  head: [
    ['link', { rel: 'icon', href: '/PulseMVI/favicon.ico' }],
  ],

  themeConfig: {
    logo: null,

    nav: [
      { text: 'Guide', link: '/guide/', activeMatch: '/guide/' },
      { text: 'API', link: '/api/', activeMatch: '/api/' },
      {
        text: 'Changelog',
        link: 'https://github.com/kaleidot725/PulseMVI/releases',
      },
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
            { text: 'Store', link: '/guide/store' },
            { text: 'Container', link: '/guide/container' },
            { text: 'Broadcast', link: '/guide/broadcast' },
            { text: 'Unicast', link: '/guide/unicast' },
          ],
        },
      ],
      '/api/': [
        {
          text: 'API Reference',
          items: [
            { text: 'Overview', link: '/api/' },
            { text: 'PulseStore', link: '/api/pulse-store' },
            { text: 'PulseContainer', link: '/api/pulse-container' },
            { text: 'Composables', link: '/api/composables' },
            { text: 'Marker Interfaces', link: '/api/interfaces' },
          ],
        },
      ],
    },

    socialLinks: [
      { icon: 'github', link: 'https://github.com/kaleidot725/PulseMVI' },
    ],

    footer: {
      message: 'Released under the Apache 2.0 License.',
      copyright: 'Copyright © 2026 kaleidot725',
    },

    editLink: {
      pattern: 'https://github.com/kaleidot725/PulseMVI/edit/main/docs/:path',
      text: 'Edit this page on GitHub',
    },

    search: {
      provider: 'local',
    },
  },
})
