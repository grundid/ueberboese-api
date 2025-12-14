/**
 * Überböse API - Main JavaScript
 * Modern minimal interactions and animations
 */

(function() {
  'use strict';

  // ===================================
  // Smooth Scrolling for Anchor Links
  // ===================================

  document.querySelectorAll('a[href^="#"]').forEach(anchor => {
    anchor.addEventListener('click', function (e) {
      e.preventDefault();
      const target = document.querySelector(this.getAttribute('href'));
      if (target) {
        target.scrollIntoView({
          behavior: 'smooth',
          block: 'start'
        });
      }
    });
  });

  // ===================================
  // Fade-in Animation on Scroll
  // ===================================

  const observerOptions = {
    threshold: 0.1,
    rootMargin: '0px 0px -50px 0px'
  };

  const fadeInObserver = new IntersectionObserver((entries) => {
    entries.forEach(entry => {
      if (entry.isIntersecting) {
        entry.target.classList.add('visible');
        // Optionally unobserve after animation to improve performance
        fadeInObserver.unobserve(entry.target);
      }
    });
  }, observerOptions);

  // Observe all elements with fade-in class
  document.querySelectorAll('.fade-in').forEach(el => {
    fadeInObserver.observe(el);
  });

  // ===================================
  // Mobile Menu Toggle
  // ===================================

  const menuToggle = document.getElementById('mobileMenuToggle');
  const mainNav = document.getElementById('mainNav');

  if (menuToggle && mainNav) {
    menuToggle.addEventListener('click', function() {
      const isExpanded = this.getAttribute('aria-expanded') === 'true';
      this.setAttribute('aria-expanded', !isExpanded);
      mainNav.classList.toggle('active');
    });

    // Close menu when clicking outside
    document.addEventListener('click', function(event) {
      const isClickInside = menuToggle.contains(event.target) || mainNav.contains(event.target);
      if (!isClickInside && mainNav.classList.contains('active')) {
        mainNav.classList.remove('active');
        menuToggle.setAttribute('aria-expanded', 'false');
      }
    });

    // Close menu when pressing Escape key
    document.addEventListener('keydown', function(event) {
      if (event.key === 'Escape' && mainNav.classList.contains('active')) {
        mainNav.classList.remove('active');
        menuToggle.setAttribute('aria-expanded', 'false');
      }
    });
  }

  // ===================================
  // Copy Code Blocks to Clipboard
  // ===================================

  document.querySelectorAll('pre code').forEach(block => {
    // Only add copy button if we have the Clipboard API
    if (navigator.clipboard) {
      const button = document.createElement('button');
      button.className = 'copy-button';
      button.textContent = 'Copy';
      button.setAttribute('aria-label', 'Copy code to clipboard');

      button.addEventListener('click', async function() {
        try {
          await navigator.clipboard.writeText(block.textContent);
          this.textContent = 'Copied!';
          setTimeout(() => {
            this.textContent = 'Copy';
          }, 2000);
        } catch (err) {
          console.error('Failed to copy:', err);
          this.textContent = 'Failed';
          setTimeout(() => {
            this.textContent = 'Copy';
          }, 2000);
        }
      });

      block.parentElement.style.position = 'relative';
      block.parentElement.appendChild(button);
    }
  });

  // ===================================
  // Add Target Blank Icons to External Links
  // ===================================

  document.querySelectorAll('a[target="_blank"]').forEach(link => {
    // Add visual indicator for external links (optional)
    if (!link.querySelector('.external-icon')) {
      link.setAttribute('rel', 'noopener noreferrer');
    }
  });

  // ===================================
  // Feature Cards Staggered Animation
  // ===================================

  const featureCards = document.querySelectorAll('.feature-card');
  featureCards.forEach((card, index) => {
    card.style.animationDelay = `${index * 0.1}s`;
  });

})();
