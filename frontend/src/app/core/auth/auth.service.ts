import { Injectable } from '@angular/core';
import { Router } from '@angular/router';
import { AuthConfig, OAuthService } from 'angular-oauth2-oidc';

import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private initialized = false;
  private initializePromise?: Promise<void>;

  private readonly authConfig: AuthConfig = {
    issuer: environment.keycloakIssuer,
    clientId: environment.keycloakClientId,
    redirectUri: window.location.origin,
    responseType: 'code',
    scope: 'openid profile email',
    showDebugInformation: false,
    strictDiscoveryDocumentValidation: false,
  };

  constructor(
    private readonly oauthService: OAuthService,
    private readonly router: Router,
  ) {}

  initialize(): Promise<void> {
    if (this.initialized) {
      return Promise.resolve();
    }

    if (this.initializePromise) {
      return this.initializePromise;
    }

    this.oauthService.configure(this.authConfig);
    this.initializePromise = this.oauthService.loadDiscoveryDocumentAndTryLogin().then(() => {
      this.initialized = true;

      if (!this.isAuthCallback()) {
        return;
      }

      const targetUrl = this.oauthService.state || '/tasks';
      return this.router.navigateByUrl(targetUrl).then(() => undefined);
    });

    return this.initializePromise;
  }

  login(returnUrl = '/tasks'): void {
    this.oauthService.initCodeFlow(returnUrl);
  }

  logout(): void {
    this.oauthService.logOut();
  }

  isAuthenticated(): boolean {
    return this.oauthService.hasValidAccessToken();
  }

  getAccessToken(): string {
    return this.oauthService.getAccessToken();
  }

  getUsername(): string {
    const claims = this.oauthService.getIdentityClaims() as Record<string, unknown> | null;
    if (claims?.['preferred_username'] && typeof claims['preferred_username'] === 'string') {
      return claims['preferred_username'];
    }

    const payload = this.getAccessTokenPayload();
    const username = payload?.['preferred_username'];
    if (typeof username === 'string' && username.length > 0) {
      return username;
    }

    return 'Unknown user';
  }

  getRoles(): string[] {
    const payload = this.getAccessTokenPayload();
    const realmAccess = payload?.['realm_access'];

    if (!realmAccess || typeof realmAccess !== 'object') {
      return [];
    }

    const roles = (realmAccess as { roles?: unknown }).roles;
    if (!Array.isArray(roles)) {
      return [];
    }

    return roles.filter((role): role is string => typeof role === 'string');
  }

  getPrimaryRole(): string {
    const roles = this.getRoles();
    if (roles.includes('ROLE_ADMIN')) {
      return 'ROLE_ADMIN';
    }
    if (roles.includes('ROLE_USER')) {
      return 'ROLE_USER';
    }
    return roles[0] ?? 'NO_ROLE';
  }

  private isAuthCallback(): boolean {
    const search = new URLSearchParams(window.location.search);
    return search.has('code') && search.has('state');
  }

  private getAccessTokenPayload(): Record<string, unknown> | null {
    const token = this.getAccessToken();
    if (!token) {
      return null;
    }

    const tokenParts = token.split('.');
    if (tokenParts.length < 2) {
      return null;
    }

    try {
      const base64 = tokenParts[1].replace(/-/g, '+').replace(/_/g, '/');
      const padded = base64 + '='.repeat((4 - (base64.length % 4)) % 4);
      return JSON.parse(atob(padded)) as Record<string, unknown>;
    } catch {
      return null;
    }
  }
}
