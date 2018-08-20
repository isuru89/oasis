import React, { Component } from 'react'
import styled from 'styled-components'
import ProfilePage from '../pages/profile/ProfilePage';
import ProgressPage from '../pages/progress/ProgressPage';
import GameCreation from '../pages/game/GameCreation';
import CreateKpiForm from '../pages/game/CreateKpiForm';
import { CreatePointForm } from '../pages/game/CreatePointForm';
import { Route } from "react-router-dom";

const Wrapper = styled.div`
  width: 100%;
  background-color: #E5F6FE;
`

export default class Content extends Component {
  render() {
    return (
      <Wrapper>
        <Route path="/profile" component={ProfilePage} />
        <Route path="/progress" component={ProgressPage} />
        
        {/* <CreatePointForm /> */}
      </Wrapper>
    )
  }
}
