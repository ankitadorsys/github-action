import { Injectable } from '@angular/core';
import { Router } from '@angular/router';
import { AuthConfig, OAuthService } from 'angular-oauth2-oidc';

import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root',
})
export class AuthService {
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
  ) {
    this.oauthService.configure(this.authConfig);
    void this.oauthService.loadDiscoveryDocumentAndTryLogin().then(() => {
      if (!this.isAuthCallback()) {
        return;
      }

      const targetUrl = this.oauthService.state || '/tasks';
      void this.router.navigateByUrl(targetUrl);
    });
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
