import React, { createContext, useContext, useEffect, useState, useMemo, useCallback } from 'react';

export type ThemePreference = 'system' | 'dark' | 'light';

export interface ThemeContextValue {
  /** The current active preference: 'system', 'dark', or 'light' */
  theme: ThemePreference;
  /** Current detected OS/system preference */
  systemPreference: 'dark' | 'light';
  /** Resolved boolean: true if dark mode is active */
  isDark: boolean;
  /** Set the theme preference */
  setTheme: (theme: ThemePreference) => void;
  /** Toggle between dark and light manually */
  toggleTheme: () => void;
}

const ThemeContext = createContext<ThemeContextValue | undefined>(undefined);

const PREFERS_DARK_QUERY = '(prefers-color-scheme: dark)';
const STORAGE_KEY = 'careerpilot_theme_preference';

/**
 * Hook to inspect whether the user's OS prefers dark mode.
 * Dynamically updates if the user switches their mobile / OS setting.
 */
export function useSystemThemePreference(): 'dark' | 'light' {
  const [preference, setPreference] = useState<'dark' | 'light'>(() => {
    if (typeof window === 'undefined' || !window.matchMedia) {
      return 'dark'; // safe default
    }
    return window.matchMedia(PREFERS_DARK_QUERY).matches ? 'dark' : 'light';
  });

  useEffect(() => {
    if (typeof window === 'undefined' || !window.matchMedia) return;

    const mediaQuery = window.matchMedia(PREFERS_DARK_QUERY);
    
    // Initial sync
    setPreference(mediaQuery.matches ? 'dark' : 'light');

    const handleMediaChange = (e: MediaQueryListEvent | MediaQueryList) => {
      setPreference(e.matches ? 'dark' : 'light');
    };

    if (mediaQuery.addEventListener) {
      mediaQuery.addEventListener('change', handleMediaChange);
    } else if (mediaQuery.addListener) {
      // Compatibility with older webviews
      mediaQuery.addListener(handleMediaChange);
    }

    return () => {
      if (mediaQuery.removeEventListener) {
        mediaQuery.removeEventListener('change', handleMediaChange);
      } else if (mediaQuery.removeListener) {
        mediaQuery.removeListener(handleMediaChange);
      }
    };
  }, []);

  return preference;
}

export interface ThemeProviderProps {
  children: React.ReactNode;
  defaultTheme?: ThemePreference;
}

/**
 * ThemeProvider detects OS preference using window.matchMedia('(prefers-color-scheme: dark)')
 * and updates the 'dark' CSS class on the document root (document.documentElement).
 */
export const ThemeProvider: React.FC<ThemeProviderProps> = ({
  children,
  defaultTheme = 'system',
}) => {
  const systemPreference = useSystemThemePreference();

  const [theme, setThemeState] = useState<ThemePreference>(() => {
    if (typeof window !== 'undefined') {
      try {
        const stored = localStorage.getItem(STORAGE_KEY) as ThemePreference | null;
        if (stored === 'system' || stored === 'dark' || stored === 'light') {
          return stored;
        }
      } catch {
        // Ignore localStorage errors (e.g. security sandbox)
      }
    }
    return defaultTheme;
  });

  // Calculate whether dark mode should be applied
  const isDark = useMemo(() => {
    if (theme === 'system') {
      return systemPreference === 'dark';
    }
    return theme === 'dark';
  }, [theme, systemPreference]);

  // Synchronize the 'dark' CSS class on document root (document.documentElement)
  useEffect(() => {
    if (typeof document === 'undefined') return;

    const root = document.documentElement;
    if (isDark) {
      root.classList.add('dark');
    } else {
      root.classList.remove('dark');
    }
  }, [isDark]);

  const setTheme = useCallback((newTheme: ThemePreference) => {
    setThemeState(newTheme);
    try {
      localStorage.setItem(STORAGE_KEY, newTheme);
    } catch {
      // Ignore storage errors
    }
  }, []);

  const toggleTheme = useCallback(() => {
    setTheme(isDark ? 'light' : 'dark');
  }, [isDark, setTheme]);

  const value = useMemo(
    () => ({
      theme,
      systemPreference,
      isDark,
      setTheme,
      toggleTheme,
    }),
    [theme, systemPreference, isDark, setTheme, toggleTheme]
  );

  return <ThemeContext.Provider value={value}>{children}</ThemeContext.Provider>;
};

/**
 * Hook to access current theme state, OS preference, and toggles.
 */
export function useTheme(): ThemeContextValue {
  const context = useContext(ThemeContext);
  if (!context) {
    throw new Error('useTheme must be used within a ThemeProvider');
  }
  return context;
}
