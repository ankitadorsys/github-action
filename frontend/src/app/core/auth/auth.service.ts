import { Injectable } from '@angular/core';
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

  constructor(private readonly oauthService: OAuthService) {
    this.oauthService.configure(this.authConfig);
    void this.oauthService.loadDiscoveryDocumentAndTryLogin();
  }

  login(): void {
    this.oauthService.initCodeFlow();
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
}
