import { render, screen, act, renderHook } from '@testing-library/react';
import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { ThemeProvider, useTheme, useSystemThemePreference } from './ThemeContext';

describe('ThemeContext & OS Preference Synchronization', () => {
  let mediaQueryListeners: Array<(e: MediaQueryListEvent | MediaQueryList) => void> = [];
  let currentMatches = true;

  beforeEach(() => {
    mediaQueryListeners = [];
    currentMatches = true;
    document.documentElement.className = '';
    localStorage.clear();

    // Mock matchMedia with dynamic event triggering capability
    window.matchMedia = vi.fn().mockImplementation((query: string) => {
      return {
        matches: currentMatches,
        media: query,
        onchange: null,
        addListener: vi.fn((cb) => mediaQueryListeners.push(cb)),
        removeListener: vi.fn((cb) => {
          mediaQueryListeners = mediaQueryListeners.filter((l) => l !== cb);
        }),
        addEventListener: vi.fn((event: string, cb: any) => {
          if (event === 'change') {
            mediaQueryListeners.push(cb);
          }
        }),
        removeEventListener: vi.fn((event: string, cb: any) => {
          if (event === 'change') {
            mediaQueryListeners = mediaQueryListeners.filter((l) => l !== cb);
          }
        }),
        dispatchEvent: vi.fn(),
      };
    });
  });

  afterEach(() => {
    document.documentElement.className = '';
    vi.clearAllMocks();
  });

  function TestConsumer() {
    const { theme, isDark, systemPreference, setTheme, toggleTheme } = useTheme();
    return (
      <div>
        <span data-testid="theme">{theme}</span>
        <span data-testid="isDark">{String(isDark)}</span>
        <span data-testid="systemPreference">{systemPreference}</span>
        <button onClick={() => setTheme('dark')} data-testid="btn-dark">
          Set Dark
        </button>
        <button onClick={() => setTheme('light')} data-testid="btn-light">
          Set Light
        </button>
        <button onClick={() => setTheme('system')} data-testid="btn-system">
          Set System
        </button>
        <button onClick={toggleTheme} data-testid="btn-toggle">
          Toggle
        </button>
      </div>
    );
  }

  it('detects OS dark mode preference via matchMedia and adds .dark class to document root', () => {
    currentMatches = true;

    render(
      <ThemeProvider>
        <TestConsumer />
      </ThemeProvider>
    );

    expect(window.matchMedia).toHaveBeenCalledWith('(prefers-color-scheme: dark)');
    expect(screen.getByTestId('systemPreference').textContent).toBe('dark');
    expect(screen.getByTestId('isDark').textContent).toBe('true');
    expect(document.documentElement.classList.contains('dark')).toBe(true);
  });

  it('detects OS light mode preference via matchMedia and removes .dark class from document root', () => {
    currentMatches = false;

    render(
      <ThemeProvider>
        <TestConsumer />
      </ThemeProvider>
    );

    expect(window.matchMedia).toHaveBeenCalledWith('(prefers-color-scheme: dark)');
    expect(screen.getByTestId('systemPreference').textContent).toBe('light');
    expect(screen.getByTestId('isDark').textContent).toBe('false');
    expect(document.documentElement.classList.contains('dark')).toBe(false);
  });

  it('automatically synchronizes document root when mobile OS switches from light to dark', () => {
    currentMatches = false;

    render(
      <ThemeProvider>
        <TestConsumer />
      </ThemeProvider>
    );

    expect(document.documentElement.classList.contains('dark')).toBe(false);

    // Simulate mobile OS switching to dark mode
    act(() => {
      currentMatches = true;
      mediaQueryListeners.forEach((listener) =>
        listener({ matches: true, media: '(prefers-color-scheme: dark)' } as any)
      );
    });

    expect(screen.getByTestId('systemPreference').textContent).toBe('dark');
    expect(screen.getByTestId('isDark').textContent).toBe('true');
    expect(document.documentElement.classList.contains('dark')).toBe(true);
  });

  it('automatically synchronizes document root when mobile OS switches from dark to light', () => {
    currentMatches = true;

    render(
      <ThemeProvider>
        <TestConsumer />
      </ThemeProvider>
    );

    expect(document.documentElement.classList.contains('dark')).toBe(true);

    // Simulate mobile OS switching to light mode
    act(() => {
      currentMatches = false;
      mediaQueryListeners.forEach((listener) =>
        listener({ matches: false, media: '(prefers-color-scheme: dark)' } as any)
      );
    });

    expect(screen.getByTestId('systemPreference').textContent).toBe('light');
    expect(screen.getByTestId('isDark').textContent).toBe('false');
    expect(document.documentElement.classList.contains('dark')).toBe(false);
  });

  it('allows explicit overrides and persists preference', () => {
    currentMatches = false; // OS is light

    render(
      <ThemeProvider>
        <TestConsumer />
      </ThemeProvider>
    );

    expect(document.documentElement.classList.contains('dark')).toBe(false);

    // Explicitly set dark
    act(() => {
      screen.getByTestId('btn-dark').click();
    });

    expect(screen.getByTestId('theme').textContent).toBe('dark');
    expect(screen.getByTestId('isDark').textContent).toBe('true');
    expect(document.documentElement.classList.contains('dark')).toBe(true);
    expect(localStorage.getItem('careerpilot_theme_preference')).toBe('dark');

    // Return back to system
    act(() => {
      screen.getByTestId('btn-system').click();
    });

    expect(screen.getByTestId('theme').textContent).toBe('system');
    // OS is light, so isDark should be false
    expect(screen.getByTestId('isDark').textContent).toBe('false');
    expect(document.documentElement.classList.contains('dark')).toBe(false);
  });

  it('throws an error if useTheme is used outside of ThemeProvider', () => {
    const consoleError = vi.spyOn(console, 'error').mockImplementation(() => {});

    expect(() => render(<TestConsumer />)).toThrow(
      'useTheme must be used within a ThemeProvider'
    );

    consoleError.mockRestore();
  });

  it('useSystemThemePreference hook returns current OS dark/light mode', () => {
    currentMatches = true;
    const { result } = renderHook(() => useSystemThemePreference());
    expect(result.current).toBe('dark');
  });
});
