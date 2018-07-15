import React, { Component } from 'react'
import styled from 'styled-components'
import ProfilePage from '../pages/profile/ProfilePage';
import ProgressPage from '../pages/progress/ProgressPage';

const Wrapper = styled.div`
  width: 100%;
  background-color: #E5F6FE;
`

export default class Content extends Component {
  render() {
    return (
      <Wrapper>
        <ProfilePage />
      </Wrapper>
    )
  }
}
