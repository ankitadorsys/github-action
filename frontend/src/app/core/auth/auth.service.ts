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

  private isAuthCallback(): boolean {
    const search = new URLSearchParams(window.location.search);
    return search.has('code') && search.has('state');
  }
}
