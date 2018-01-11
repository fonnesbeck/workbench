import {Component, OnInit} from '@angular/core';
import {Router} from '@angular/router';

import {IdVerificationRequest, ProfileService} from 'generated';
import {ProfileEditComponent} from '../profile-edit/component';
import {Profile} from '../../../generated';

function isBlank(s: string) {
  return (!s || /^\s*$/.test(s));
}
@Component({
  styleUrls: ['./component.css'],
  templateUrl: './component.html',
})
export class IdVerificationPageComponent implements OnInit {
  request: IdVerificationRequest;
    allFieldsReq : boolean
  constructor(
      private profileService: ProfileService,
      private router: Router
  ) {}

  ngOnInit(): void {
      this.allFieldsReq = false;
    this.request = {
      firstName: '',
      lastName: '',
      streetLine1: '',
      streetLine2: '',
      city: '',
      state: '',
      zip: '',
      dob: '',
      documentType: '',
      documentNumber: ''
    };
    this.profileService.getMe().subscribe(
      (profile: Profile) => {
        this.request.firstName = profile.givenName;
        this.request.lastName = profile.familyName
      });
  }

  submit(): void {
    const requiredFields =
      [this.request.firstName, this.request.lastName,
          this.request.streetLine1, this.request.streetLine2, this.request.city,
          this.request.state, this.request.zip, this.request.dob, this.request.documentNumber,
          this.request.documentType];
    if(requiredFields.some(isBlank))
    {
      this.allFieldsReq=true;
      return;
    }
    this.allFieldsReq = false;
    let dobFormat = this.request.dob;
    let dobArr = dobFormat.split("/");
    dobFormat = dobArr[2]+"-"+dobArr[1]+"-"+dobArr[0];
    this.request.dob = dobFormat;
    this.profileService.submitIdVerification(this.request).subscribe(() => {
      this.router.navigate(['profile']);
    },error =>{
        this.router.navigate(['profile']);
    });
  }
}
