import { NextResponse } from 'next/server';
import type { NextRequest } from 'next/server';

function isTokenExpired(token: string | undefined) {
    if (!token) return true;
    try {
        const payload = JSON.parse(Buffer.from(token.split('.')[1], 'base64').toString());
        return payload.exp * 1000 < Date.now();
    } catch {
        return true;
    }
}

export default function middleware(request: NextRequest) {
    const { pathname } = request.nextUrl;
    // Sadece dashboard ve alt route'lar için kontrol
    if (pathname.startsWith('/dashboard')) {
        // Token'ı cookie'den al
        const token = request.cookies.get('accessToken')?.value;
        if (!token || isTokenExpired(token)) {
            return NextResponse.redirect(new URL('/login', request.url));
        }
    }
    if (pathname === '/') {
        return NextResponse.redirect(new URL('/login', request.url));
    }
}

// export const config = {
//     matcher: ['/', '/dashboard/:path*']
// };

