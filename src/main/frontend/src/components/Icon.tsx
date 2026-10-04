export type IconName = 'stock' | 'box' | 'users' | 'key' | 'logout' | 'refresh' | 'plus' | 'arrow' | 'mail' | 'check' | 'eye' | 'eyeOff' | 'search';
const paths: Record<IconName, string> = {
  stock: 'M4 20V10m5 10V4m6 16v-7m5 7V7M2 20h20',
  box: 'm12 3 9 5-9 5-9-5 9-5Zm-9 5v10l9 5 9-5V8M12 13v10M7.5 5.5l9 5',
  users: 'M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2M16 3a4 4 0 0 1 0 8M22 21v-2a4 4 0 0 0-3-3.87M13 7a4 4 0 1 1-8 0 4 4 0 0 1 8 0Z',
  key: 'M10 14a6 6 0 1 1 3-3l8-8M17 7l3 3M19 5l3 3',
  logout: 'M9 3H4v18h5M12 12h10m-4-4 4 4-4 4',
  refresh: 'M20 7v5h-5M4 17v-5h5M6 6a8 8 0 0 1 13 2M5 16a8 8 0 0 0 13 2',
  plus: 'M12 5v14M5 12h14',
  arrow: 'M4 12h16m-6-6 6 6-6 6',
  mail: 'M3 5h18v14H3V5Zm0 0 9 8 9-8',
  check: 'm5 12 4 4 10-10',
  eye: 'M2 12s3-7 10-7 10 7 10 7-3 7-10 7S2 12 2 12Zm13 0a3 3 0 1 1-6 0 3 3 0 0 1 6 0Z',
  eyeOff: 'm3 3 18 18M10 5a12 12 0 0 1 12 7 16 16 0 0 1-4 5M6 6a16 16 0 0 0-4 6s3 7 10 7a12 12 0 0 0 5-1M10 10a3 3 0 0 0 4 4',
  search: 'M11 18a7 7 0 1 1 0-14 7 7 0 0 1 0 14Zm5-2 5 5',
};
export default function Icon({ name, className = '' }: { name: IconName; className?: string }) {
  return <svg className={`icon ${className}`} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.7" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true" focusable="false"><path d={paths[name]} /></svg>;
}
