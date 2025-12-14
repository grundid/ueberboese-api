---
layout: default
title: Home
description: Keep your Bose SoundTouch devices alive
---

<div class="hero">
  <div class="hero-content">
    <h1>Keep Your Bose SoundTouch Alive</h1>
    <p class="hero-subtitle">A reverse-engineered API to prevent millions of working devices from becoming e-waste</p>
    <a href="{{ '/quick-start' | relative_url }}" class="cta-button">Get Started</a>
  </div>
</div>

<section class="section fade-in">
  <h2 class="section-title">The Problem</h2>
  <div style="max-width: 800px; margin: 0 auto;">
    <p style="font-size: 1.25rem; text-align: center; color: var(--color-text-light);">
      Bose has announced the end of life for its consumer streaming boxes called SoundTouch. 
      This will render <strong>millions of completely working streaming boxes useless</strong>.
    </p>
    <p style="text-align: center; margin-top: 2rem;">
      Despite being fully functional hardware, these devices will stop working when Bose shuts down their streaming API servers. 
      This creates unnecessary electronic waste and demonstrates the problem with proprietary cloud-dependent devices.
    </p>
  </div>
</section>

<section class="section section-alt fade-in">
  <h2 class="section-title">The Solution: Überböse API</h2>
  <div style="max-width: 800px; margin: 0 auto;">
    <p style="font-size: 1.125rem; text-align: center;">
      <strong>Überböse</strong> <em>(/ˈyːbɐˌbøːzə/ - German: extremely or supremely evil; beyond ordinary wickedness)</em> 😈
    </p>
    <p style="text-align: center; margin-top: 1.5rem;">
      This project reverse-engineers and reimplements the Bose streaming HTTP API, giving you control over your devices. 
      Self-host the API, configure your devices once, and keep them running indefinitely.
    </p>
    <ul style="margin-top: 2rem; list-style: none; padding: 0;">
      <li style="padding: 0.5rem 0;">✅ <strong>Self-hosted</strong> - Deploy on your own infrastructure with Docker</li>
      <li style="padding: 0.5rem 0;">✅ <strong>Open source</strong> - Community-driven development and transparency</li>
      <li style="padding: 0.5rem 0;">✅ <strong>Keep devices working</strong> - Preserve functional hardware from obsolescence</li>
    </ul>
  </div>
</section>

<section class="section fade-in">
  <h2 class="section-title">Features</h2>
  <div class="features-grid">
    <div class="feature-card">
      <span class="feature-icon">🚀</span>
      <h3>Easy Installation</h3>
      <p>Deploy with Docker Compose in minutes. Simple configuration with environment variables and persistent data storage.</p>
    </div>
    <div class="feature-card">
      <span class="feature-icon">🎵</span>
      <h3>Spotify Support</h3>
      <p>OAuth integration for Spotify. Configure your credentials and keep streaming your favorite music.</p>
    </div>
    <div class="feature-card">
      <span class="feature-icon">🔒</span>
      <h3>Self-Hosted</h3>
      <p>Keep control over your data. Runs in your local network with your infrastructure.</p>
    </div>
  </div>
</section>

<section class="section section-alt fade-in">
  <h2 class="section-title">Ready to Get Started?</h2>
  <div style="text-align: center;">
    <p style="font-size: 1.125rem; margin-bottom: 2rem;">
      Follow our step-by-step Quick Start guide to deploy Überböse API and configure your SoundTouch devices.
    </p>
    <a href="{{ '/quick-start' | relative_url }}" class="cta-button">View Quick Start Guide</a>
  </div>
</section>
