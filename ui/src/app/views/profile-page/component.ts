import {Component, OnInit} from '@angular/core';

import {ErrorHandlingService} from 'app/services/error-handling.service';
import {SignInService} from 'app/services/sign-in.service';
import {Profile, ProfileService} from 'generated';

@Component({
  styleUrls: ['./component.css'],
  templateUrl: './component.html',
})
export class ProfilePageComponent implements OnInit {
  verifiedStatusIsLoaded: boolean;
  verifiedStatusIsValid: boolean;
  profile: Profile;
  profileLoaded = false;
  editHover = false;
  constructor(
      private errorHandlingService: ErrorHandlingService,
      private profileService: ProfileService,
      private signInService: SignInService,
  ) {}

  ngOnInit(): void {
    this.getVerifiedStatus();
  }

  getVerifiedStatus(): void {
    this.errorHandlingService.retryApi(this.profileService.getMe()).subscribe(
        (profile: Profile) => {
      console.log(profile);
      this.verifiedStatusIsValid = profile.blockscoreVerificationIsValid;
      this.verifiedStatusIsLoaded = true;
      this.profile = profile;
      this.profileLoaded = true;
    });
  }

  deleteAccount(): void {
    this.profileService.deleteAccount().subscribe(() => {
      this.signInService.signOut();
    });
  }

  submitTermsOfService(): void {
    this.profileService.submitTermsOfService().subscribe();
  }


  completeEthicsTraining(): void {
    this.profileService.completeEthicsTraining().subscribe();
  }


  submitDemographicSurvey(): void {
    this.profileService.submitDemographicsSurvey().subscribe();
  }
}