import {Location} from '@angular/common';
import {Component, OnInit} from '@angular/core';
import {Title} from '@angular/platform-browser';
import {
  ActivatedRoute,
  Event as RouterEvent,
  NavigationEnd,
  Router,
} from '@angular/router';

import {Observable} from 'rxjs/Observable';

import {ErrorHandlingService} from 'app/services/error-handling.service';
import {SignInDetails, SignInService} from 'app/services/sign-in.service';
import {environment} from 'environments/environment';

import {Authority, ProfileService} from 'generated';

declare const gapi: any;
export const overriddenUrlKey = 'allOfUsApiUrlOverride';

@Component({
  selector: 'app-aou',
  styleUrls: ['./component.css'],
  templateUrl: './component.html'
})
export class AppComponent implements OnInit {
  user: Observable<SignInDetails>;
  hasReviewResearchPurpose = false;
  hasReviewIdVerification = false;
  currentUrl: string;
  email: string;

  private baseTitle: string;
  private overriddenUrl: string = null;
  private showCreateAccount = false;

  constructor(
    /* Ours */
    private signInService: SignInService,
    private errorHandlingService: ErrorHandlingService,
    private profileService: ProfileService,
    /* Angular's */
    private activatedRoute: ActivatedRoute,
    private locationService: Location,
    private router: Router,
    private titleService: Title,
  ) {}

  ngOnInit(): void {
    this.overriddenUrl = localStorage.getItem(overriddenUrlKey);
    window['setAllOfUsApiUrl'] = (url: string) => {
      if (url) {
        if (!url.match(/^https?:[/][/][a-z0-9.:-]+$/)) {
          throw new Error('URL should be of the form "http[s]://host.example.com[:port]"');
        }
        this.overriddenUrl = url;
        localStorage.setItem(overriddenUrlKey, url);
      } else {
        this.overriddenUrl = null;
        localStorage.removeItem(overriddenUrlKey);
      }
      window.location.reload();
    };
    console.log('To override the API URL, try:\n' +
      'setAllOfUsApiUrl(\'https://host.example.com:1234\')');

    // Pick up the global site title from HTML, and (for non-prod) add a tag
    // naming the current environment.
    this.baseTitle = this.titleService.getTitle();
    if (environment.displayTag) {
      this.baseTitle = `[${environment.displayTag}] ${this.baseTitle}`;
      this.titleService.setTitle(this.baseTitle);
    }

    this.router.events.subscribe((event: RouterEvent) => {
      this.setTitleFromRoute(event);
    });

    this.user = this.signInService.user;
    this.user.subscribe(user => {
      if (user.isSignedIn) {
        this.errorHandlingService.retryApi(this.profileService.getMe()).subscribe(profile => {
          this.hasReviewResearchPurpose =
            profile.authorities.includes(Authority.REVIEWRESEARCHPURPOSE);
          this.hasReviewIdVerification =
            profile.authorities.includes(Authority.REVIEWIDVERIFICATION);
            // this.email = profile.username;
        });
      }
    });
  }

  /**
   * Uses the title service to set the page title after nagivation events
   */
  private setTitleFromRoute(event: RouterEvent): void {
    this.currentUrl = this.router.url;
    if (event instanceof NavigationEnd) {

      let currentRoute = this.activatedRoute;
      while (currentRoute.firstChild) {
        currentRoute = currentRoute.firstChild;
      }
      if (currentRoute.outlet === 'primary') {
        currentRoute.data.subscribe(value =>
            this.titleService.setTitle(`${value.title} | ${this.baseTitle}`));
      }
    }
  }

  signIn(e: Event): void {
    this.signInService.signIn();
  }

  signOut(e: Event): void {
    this.signInService.signOut();
  }

  getTopMargin(): string {
    return this.showCreateAccount ? '10vh' : '30vh';
  }

  get reviewActive(): boolean {
    return this.locationService.path().startsWith('/review');
  }

  get workspacesActive(): boolean {
    return this.locationService.path() === ''
      || this.locationService.path().startsWith('/workspace');
  }
}
