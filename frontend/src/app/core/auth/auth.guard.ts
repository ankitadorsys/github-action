import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';

import { AuthService } from './auth.service';

export const authGuard: CanActivateFn = (_route, state) => {
  if (_route.queryParamMap.has('code') && _route.queryParamMap.has('state')) {
    return true;
  }

  const authService = inject(AuthService);
  const router = inject(Router);

  return authService.initialize().then(() => {
    if (authService.isAuthenticated()) {
      return true;
    }

    return router.createUrlTree(['/login'], {
      queryParams: { returnUrl: state.url },
    });
  });
};
